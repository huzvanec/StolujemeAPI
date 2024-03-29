package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import cz.jeme.programu.stolujemeapi.db.StoluStatementWrapper;
import cz.jeme.programu.stolujemeapi.rest.control.RegisterController;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class StoluUserDao implements UserDao {
    private final @NotNull Database database;
    private final @NotNull StatementWrapper wrapper = new StoluStatementWrapper();

    public StoluUserDao(final @NotNull Database database) {
        this.database = database;
    }

    @Override
    public void init() {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
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
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize user data access object!", e);
        }
    }

    @Override
    public @NotNull Optional<User> getById(final int id) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT email, name, verified, registered_timestamp, password_hash, password_salt
                    FROM users WHERE user_id = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluUser(
                    id,
                    result.getString(1),
                    result.getString(2),
                    result.getBoolean(3),
                    result.getTimestamp(4).toLocalDateTime(),
                    result.getString(5),
                    result.getString(6)
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find user by id!", e);
        }
    }

    @Override
    public @NotNull Optional<User> getByEmail(final @NotNull String email) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT user_id, name, verified, registered_timestamp, password_hash, password_salt
                    FROM users WHERE email = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(email)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluUser(
                    result.getInt(1),
                    email,
                    result.getString(2),
                    result.getBoolean(3),
                    result.getTimestamp(4).toLocalDateTime(),
                    result.getString(5),
                    result.getString(6)
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find user by email!", e);
        }
    }

    @Override
    public @NotNull Optional<User> getByName(final @NotNull String name) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT user_id, email, verified, registered_timestamp, password_hash, password_salt
                    FROM users WHERE name = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(name)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluUser(
                    result.getInt(1),
                    result.getString(2),
                    name,
                    result.getBoolean(3),
                    result.getTimestamp(4).toLocalDateTime(),
                    result.getString(5),
                    result.getString(6)
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find user by name!", e);
        }
    }


    @Override
    public boolean existsId(final int id) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM users WHERE user_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for user id!", e);
        }
    }

    @Override
    public boolean existsEmail(final @NotNull String email) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM users WHERE email = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(email)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for user email!", e);
        }
    }

    @Override
    public boolean existsName(final @NotNull String name) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM users WHERE name = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(name)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for user name!", e);
        }
    }


    @Override
    public @NotNull User insert(final @NotNull UserSkeleton skeleton) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    INSERT INTO users (email, name, registered_timestamp, password_hash, password_salt)
                    VALUES (?, ?, ?, ?, ?);
                    """;
            LocalDateTime registered = LocalDateTime.now();
            PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setString(skeleton.email())
                    .setString(skeleton.name())
                    .setTimestamp(Timestamp.valueOf(registered))
                    .setString(skeleton.passwordHash())
                    .setString(skeleton.passwordSalt())
                    .unwrap();
            statement.execute();
            ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("User id was not returned!");
            return new StoluUser(
                    result.getInt(1),
                    skeleton.email(),
                    skeleton.name(),
                    false,
                    registered,
                    skeleton.passwordHash(),
                    skeleton.passwordSalt()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Could not register user!", e);
        }
    }
}
