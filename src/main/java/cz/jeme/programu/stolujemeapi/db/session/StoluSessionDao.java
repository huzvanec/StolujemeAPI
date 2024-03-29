package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import cz.jeme.programu.stolujemeapi.db.StoluStatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class StoluSessionDao implements SessionDao {
    private final @NotNull Database database;
    private final @NotNull StatementWrapper wrapper = new StoluStatementWrapper();

    public StoluSessionDao(final @NotNull Database database) {
        this.database = database;
    }

    @Override
    public void init() {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    CREATE TABLE IF NOT EXISTS sessions (
                    session_id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    user_id MEDIUMINT UNSIGNED NOT NULL,
                    creation_timestamp DATETIME NOT NULL,
                    expiration_timestamp DATETIME NOT NULL,
                    token VARCHAR(%d) NOT NULL UNIQUE,
                    CONSTRAINT `fk_session_user`
                        FOREIGN KEY (user_id) REFERENCES users (user_id)
                    );
                    """
                    .formatted(CryptoUtils.TOKEN_LENGTH_BASE64);
            connection.prepareStatement(statementStr).execute();
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize session data access object!", e);
        }
    }

    @Override
    public @NotNull Optional<Session> getById(final int id) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT user_id, creation_timestamp, expiration_timestamp, token
                    FROM sessions WHERE session_id = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluSession(
                    id,
                    result.getInt(1),
                    result.getTimestamp(2).toLocalDateTime(),
                    result.getTimestamp(3).toLocalDateTime(),
                    result.getString(4)
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find session by id!", e);
        }
    }


    @Override
    public @NotNull Optional<Session> getByToken(final @NotNull String token) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT session_id, user_id, creation_timestamp, expiration_timestamp
                    FROM sessions WHERE token = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(token)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluSession(
                    result.getInt(1),
                    result.getInt(2),
                    result.getTimestamp(3).toLocalDateTime(),
                    result.getTimestamp(4).toLocalDateTime(),
                    token
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find session by token!", e);
        }
    }

    @Override
    public boolean existsId(final int id) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM sessions WHERE session_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for session id!", e);
        }
    }


    @Override
    public boolean existsToken(final @NotNull String token) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM sessions WHERE token = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(token)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for session token!", e);
        }
    }

    @Override
    public @NotNull Session insert(final @NotNull SessionSkeleton skeleton) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    INSERT INTO sessions (user_id, creation_timestamp, expiration_timestamp, token)
                    VALUES (?, ?, ?, ?);
                    """;
            LocalDateTime creation = LocalDateTime.now();
            LocalDateTime expiration = creation.plus(skeleton.duration());
            PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.userId())
                    .setTimestamp(Timestamp.valueOf(creation))
                    .setTimestamp(Timestamp.valueOf(expiration))
                    .setString(skeleton.token())
                    .unwrap();
            statement.execute();
            ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Session id was not returned!");
            return new StoluSession(
                    result.getInt(1),
                    skeleton.userId(),
                    creation,
                    expiration,
                    skeleton.token()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Could not create session!", e);
        }
    }
}
