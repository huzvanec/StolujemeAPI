package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import cz.jeme.programu.stolujemeapi.rest.control.RegisterController;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public enum UserDao implements Dao<User, UserSkeleton> {
    INSTANCE;

    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();
    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    CREATE TABLE IF NOT EXISTS users (
                    user_id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(%d) NOT NULL UNIQUE,
                    name VARCHAR(%d) NOT NULL UNIQUE,
                    verified BOOLEAN NOT NULL DEFAULT FALSE,
                    registered_timestamp DATETIME NOT NULL,
                    password_hash VARCHAR(%d) NOT NULL,
                    password_salt VARCHAR(%d) NOT NULL
                    );
                    """
                    .formatted(
                            RegisterController.EMAIL_LENGTH_MAX,
                            RegisterController.NAME_LENGTH_MAX,
                            CryptoUtils.KEY_LENGTH_BASE64,
                            CryptoUtils.SALT_LENGTH_BASE64
                    );
            connection.prepareStatement(statementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize user data access object!", e);
        }
    }

    @Override
    public @NotNull Optional<User> byId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT email, name, verified, registered_timestamp, password_hash, password_salt
                    FROM users WHERE user_id = ?;
                    """;
            final ResultSet result = wrapper
                    .wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new User.Builder()
                    .id(id)
                    .email(result.getString(1))
                    .name(result.getString(2))
                    .verified(result.getBoolean(3))
                    .registered(result.getTimestamp(4).toLocalDateTime())
                    .passwordHash(result.getString(5))
                    .passwordSalt(result.getString(6))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find user by id!", e);
        }
    }

    public @NotNull Optional<User> byEmail(final @NotNull String email) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT user_id, name, verified, registered_timestamp, password_hash, password_salt
                    FROM users WHERE email = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(email)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new User.Builder()
                    .id(result.getInt(1))
                    .email(email)
                    .name(result.getString(2))
                    .verified(result.getBoolean(3))
                    .registered(result.getTimestamp(4).toLocalDateTime())
                    .passwordHash(result.getString(5))
                    .passwordSalt(result.getString(6))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find user by email!", e);
        }
    }

    public @NotNull Optional<User> byName(final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT user_id, email, verified, registered_timestamp, password_hash, password_salt
                    FROM users WHERE name = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(name)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new User.Builder()
                    .id(result.getInt(1))
                    .email(result.getString(2))
                    .name(name)
                    .verified(result.getBoolean(3))
                    .registered(result.getTimestamp(4).toLocalDateTime())
                    .passwordHash(result.getString(5))
                    .passwordSalt(result.getString(6))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find user by name!", e);
        }
    }


    @Override
    public boolean existsId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM users WHERE user_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for user id!", e);
        }
    }

    public boolean existsEmail(final @NotNull String email) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM users WHERE email = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(email)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for user email!", e);
        }
    }

    public boolean existsName(final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM users WHERE name = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(name)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for user name!", e);
        }
    }


    @Override
    public @NotNull User insert(final @NotNull UserSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO users (email, name, registered_timestamp, password_hash, password_salt)
                    VALUES (?, ?, ?, ?, ?);
                    """;
            final LocalDateTime registered = LocalDateTime.now();
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setString(skeleton.email())
                    .setString(skeleton.name())
                    .setTimestamp(Timestamp.valueOf(registered))
                    .setString(skeleton.passwordHash())
                    .setString(skeleton.passwordSalt())
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("User id was not returned!");
            return new User.Builder()
                    .id(result.getInt(1))
                    .email(skeleton.email())
                    .name(skeleton.name())
                    .verified(false)
                    .registered(registered)
                    .passwordHash(skeleton.passwordHash())
                    .passwordSalt(skeleton.passwordSalt())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not register user!", e);
        }
    }
}