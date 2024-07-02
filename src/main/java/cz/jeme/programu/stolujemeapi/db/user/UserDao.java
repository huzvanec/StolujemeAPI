package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.rest.control.UserController;
import cz.jeme.programu.stolujemeapi.sql.ResultWrapper;
import cz.jeme.programu.stolujemeapi.sql.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public enum UserDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String registrationsStatementStr = """
                    CREATE TABLE IF NOT EXISTS registrations
                    (
                        id_registration MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        creation_time   DATETIME    NOT NULL,
                        expiration_time DATETIME    NOT NULL,
                        email           VARCHAR(%d) NOT NULL,
                        name            VARCHAR(%d) NOT NULL,
                        canteen         VARCHAR(30) NOT NULL,
                        password_hash   VARCHAR(%d) NOT NULL,
                        password_salt   VARCHAR(%d) NOT NULL,
                        code            VARCHAR(%d) NOT NULL UNIQUE
                    );
                    """
                    .formatted(
                            UserController.EMAIL_LENGTH_MAX,
                            UserController.NAME_LENGTH_MAX,
                            CryptoUtils.KEY_LENGTH_BASE64,
                            CryptoUtils.SALT_LENGTH_BASE64,
                            CryptoUtils.VERIFICATION_LENGTH_BASE64
                    );
            connection.prepareStatement(registrationsStatementStr).execute();
            // language=mariadb
            final String usersStatementStr = """
                    CREATE TABLE IF NOT EXISTS users
                    (
                        id_user         MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        id_registration MEDIUMINT UNSIGNED UNIQUE NOT NULL,
                        FOREIGN KEY (id_registration) REFERENCES registrations (id_registration),
                        email           VARCHAR(%d)               NOT NULL UNIQUE,
                        name            VARCHAR(%d)               NOT NULL UNIQUE,
                        canteen         VARCHAR(30)               NOT NULL,
                        creation_time   DATETIME                  NOT NULL,
                        password_hash   VARCHAR(%d)               NOT NULL,
                        password_salt   VARCHAR(%d)               NOT NULL
                    );
                    """
                    .formatted(
                            UserController.EMAIL_LENGTH_MAX,
                            UserController.NAME_LENGTH_MAX,
                            CryptoUtils.KEY_LENGTH_BASE64,
                            CryptoUtils.SALT_LENGTH_BASE64
                    );
            connection.prepareStatement(usersStatementStr).execute();
            // language=mariadb
            final String sessionsStatementStr = """
                    CREATE TABLE IF NOT EXISTS sessions
                    (
                        id_session      MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        id_user         MEDIUMINT UNSIGNED NOT NULL,
                        FOREIGN KEY (id_user) REFERENCES users (id_user),
                        creation_time   DATETIME           NOT NULL,
                        expiration_time DATETIME           NOT NULL,
                        token           VARCHAR(%d)        NOT NULL UNIQUE
                    );
                    """
                    .formatted(CryptoUtils.SESSION_LENGTH_BASE64);
            connection.prepareStatement(sessionsStatementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize user data access object!", e);
        }
    }

    // USER

    public boolean existsUserEmail(final @NotNull String email) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1
                    FROM users
                    WHERE email = ?;
                    """;
            return StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(email)
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not search for user!", e);
        }
    }

    public boolean existsUserName(final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1
                    FROM users
                    WHERE name = ?;
                    """;
            return StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(name)
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not search for user!", e);
        }
    }

    public @NotNull Optional<User> userById(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_registration, email, name, canteen, creation_time, password_hash, password_salt
                    FROM users
                    WHERE id_user = ?;
                    """;

            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .executeQuery();

            if (!result.next()) return Optional.empty();

            return Optional.of(new User.Builder()
                    .id(id)
                    .registrationId(result.getInt())
                    .email(result.getString())
                    .name(result.getString())
                    .canteen(result.getCanteen())
                    .creationTime(result.getLocalDateTime())
                    .passwordHash(result.getString())
                    .passwordSalt(result.getString())
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not get registration!", e);
        }
    }

    public @NotNull User userBySession(final @NotNull Session session) {
        return userById(session.userId())
                .orElseThrow(() -> new RuntimeException("Could not find user from session!"));
    }

    public @NotNull Optional<User> userByEmail(final @NotNull String email) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_user, id_registration, name, canteen, creation_time, password_hash, password_salt
                    FROM users
                    WHERE email = ?;
                    """;

            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(email)
                    .executeQuery();

            if (!result.next()) return Optional.empty();

            return Optional.of(new User.Builder()
                    .id(result.getInt())
                    .registrationId(result.getInt())
                    .email(email)
                    .name(result.getString())
                    .canteen(result.getCanteen())
                    .creationTime(result.getLocalDateTime())
                    .passwordHash(result.getString())
                    .passwordSalt(result.getString())
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not get registration!", e);
        }
    }

    // REGISTRATION

    public @NotNull Optional<Registration> activeRegistrationByEmailAndName(final @NotNull String email,
                                                                            final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_registration, creation_time, expiration_time, canteen, password_hash, password_salt, code
                    FROM registrations
                    WHERE email = ?
                      AND name = ?
                      AND expiration_time > CURRENT_TIMESTAMP
                    LIMIT 1;
                    """;

            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(email)
                    .setString(name)
                    .executeQuery();

            if (!result.next()) return Optional.empty();

            return Optional.of(new Registration.Builder()
                    .id(result.getInt())
                    .creationTime(result.getLocalDateTime())
                    .expirationTime(result.getLocalDateTime())
                    .email(email)
                    .name(name)
                    .canteen(result.getCanteen())
                    .passwordHash(result.getString())
                    .passwordSalt(result.getString())
                    .code(result.getString())
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not get registration!", e);
        }
    }

    public @NotNull Registration insertRegistration(final @NotNull RegistrationSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO registrations (creation_time, expiration_time, email, name, canteen, password_hash, password_salt, code)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                    """;
            final LocalDateTime creationTime = LocalDateTime.now();
            final LocalDateTime expirationTime = creationTime.plus(skeleton.duration());
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(
                            statementStr,
                            Statement.RETURN_GENERATED_KEYS
                    ))
                    .setLocalDateTime(creationTime)
                    .setLocalDateTime(expirationTime)
                    .setString(skeleton.email())
                    .setString(skeleton.name())
                    .setString(skeleton.canteen().name())
                    .setString(skeleton.passwordHash())
                    .setString(skeleton.passwordSalt())
                    .setString(skeleton.code())
                    .executeGenerate();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Registration.Builder()
                    .id(result.getInt())
                    .creationTime(creationTime)
                    .expirationTime(expirationTime)
                    .email(skeleton.email())
                    .name(skeleton.name())
                    .canteen(skeleton.canteen())
                    .passwordHash(skeleton.passwordHash())
                    .passwordSalt(skeleton.passwordSalt())
                    .code(skeleton.code())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create registration!", e);
        }
    }

    public @NotNull Optional<Registration> registrationByCode(final @NotNull String code) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_registration,
                           creation_time,
                           expiration_time,
                           email,
                           name,
                           canteen,
                           password_hash,
                           password_salt
                    FROM registrations
                    WHERE code = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(code)
                    .executeQuery();

            if (!result.next()) return Optional.empty();

            return Optional.of(new Registration.Builder()
                    .id(result.getInt())
                    .creationTime(result.getLocalDateTime())
                    .expirationTime(result.getLocalDateTime())
                    .email(result.getString())
                    .name(result.getString())
                    .canteen(result.getCanteen())
                    .passwordHash(result.getString())
                    .passwordSalt(result.getString())
                    .code(code)
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not get registration!", e);
        }
    }

    public @NotNull User register(final @NotNull Registration registration) {
        try (final Connection connection = database.connection()) {
            final StatementWrapper wrapper = StatementWrapper.wrapper();
            // language=mariadb
            final String registrationStatementStr = """
                    UPDATE registrations
                    SET expiration_time = CURRENT_TIMESTAMP
                    WHERE email = ?
                      AND expiration_time > CURRENT_TIMESTAMP;
                    """;
            wrapper.wrap(connection.prepareStatement(registrationStatementStr))
                    .setString(registration.email())
                    .executeUpdate();

            // language=mariadb
            final String userStatementStr = """
                    INSERT INTO users (id_registration, email, name, canteen, creation_time, password_hash, password_salt)
                    VALUES (?, ?, ?, ?, ?, ?, ?);
                    """;

            final LocalDateTime creationTime = LocalDateTime.now();
            final ResultWrapper result = wrapper.wrap(connection.prepareStatement(userStatementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(registration.id())
                    .setString(registration.email())
                    .setString(registration.name())
                    .setString(registration.canteen().name())
                    .setLocalDateTime(creationTime)
                    .setString(registration.passwordHash())
                    .setString(registration.passwordSalt())
                    .executeGenerate();

            if (!result.next()) throw new RuntimeException("Id was not returned!");

            return new User.Builder()
                    .id(result.getInt())
                    .registrationId(registration.id())
                    .email(registration.email())
                    .name(registration.name())
                    .creationTime(creationTime)
                    .passwordHash(registration.passwordHash())
                    .passwordSalt(registration.passwordSalt())
                    .canteen(registration.canteen())
                    .build();

        } catch (final SQLException e) {
            throw new RuntimeException("Could not register user!", e);
        }
    }

    // SESSION

    public @NotNull Optional<Session> sessionByToken(final @NotNull String token) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_session, id_user, creation_time, expiration_time
                    FROM sessions
                    WHERE token = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(token)
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Session.Builder()
                    .id(result.getInt())
                    .userId(result.getInt())
                    .creationTime(result.getLocalDateTime())
                    .expirationTime(result.getLocalDateTime())
                    .token(token)
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not get session!", e);
        }
    }

    public @NotNull Session insertSession(final @NotNull SessionSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO sessions (id_user, creation_time, expiration_time, token)
                    VALUES (?, ?, ?, ?);
                    """;
            final LocalDateTime creationTime = LocalDateTime.now();
            final LocalDateTime expirationTime = creationTime.plus(skeleton.duration());
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(
                            statementStr,
                            Statement.RETURN_GENERATED_KEYS
                    ))
                    .setInt(skeleton.userId())
                    .setLocalDateTime(creationTime)
                    .setLocalDateTime(expirationTime)
                    .setString(skeleton.token())
                    .executeGenerate();

            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Session.Builder()
                    .id(result.getInt())
                    .userId(skeleton.userId())
                    .creationTime(creationTime)
                    .expirationTime(expirationTime)
                    .token(skeleton.token())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create session!", e);
        }
    }

    public boolean endSession(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    UPDATE sessions
                    SET expiration_time = CURRENT_TIMESTAMP
                    WHERE id_session = ?;
                    """;
            return StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                           .setInt(id)
                           .executeUpdate() > 0;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not end session!", e);
        }
    }
}