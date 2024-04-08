package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public enum SessionDao implements Dao<Session, SessionSkeleton> {
    INSTANCE;

    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();
    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
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
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize session data access object!", e);
        }
    }

    @Override
    public @NotNull Optional<Session> byId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT user_id, creation_timestamp, expiration_timestamp, token
                    FROM sessions WHERE session_id = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Session.Builder()
                    .id(id)
                    .userId(result.getInt(1))
                    .creation(result.getTimestamp(2).toLocalDateTime())
                    .expiration(result.getTimestamp(3).toLocalDateTime())
                    .token(result.getString(4))
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find session by id!", e);
        }
    }


    public @NotNull Optional<Session> byToken(final @NotNull String token) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT session_id, user_id, creation_timestamp, expiration_timestamp
                    FROM sessions WHERE token = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(token)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Session.Builder()
                    .id(result.getInt(1))
                    .userId(result.getInt(2))
                    .creation(result.getTimestamp(3).toLocalDateTime())
                    .expiration(result.getTimestamp(4).toLocalDateTime())
                    .token(token)
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find session by token!", e);
        }
    }

    @Override
    public boolean existsId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM sessions WHERE session_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for session id!", e);
        }
    }


    public boolean existsToken(final @NotNull String token) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM sessions WHERE token = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(token)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for session token!", e);
        }
    }

    @Override
    public @NotNull Session insert(final @NotNull SessionSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO sessions (user_id, creation_timestamp, expiration_timestamp, token)
                    VALUES (?, ?, ?, ?);
                    """;
            final LocalDateTime creation = LocalDateTime.now();
            final LocalDateTime expiration = creation.plus(skeleton.duration());
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.userId())
                    .setTimestamp(Timestamp.valueOf(creation))
                    .setTimestamp(Timestamp.valueOf(expiration))
                    .setString(skeleton.token())
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Session id was not returned!");
            return new Session.Builder()
                    .id(result.getInt(1))
                    .userId(skeleton.userId())
                    .creation(creation)
                    .expiration(expiration)
                    .token(skeleton.token())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create session!", e);
        }
    }
}