package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public enum SessionDao implements Dao {
    INSTANCE;

    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();
    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    CREATE TABLE IF NOT EXISTS sessions (
                    id_session MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_user MEDIUMINT UNSIGNED NOT NULL,
                    creation_time DATETIME NOT NULL,
                    expiration_time DATETIME NOT NULL,
                    token VARCHAR(%d) NOT NULL UNIQUE,
                    CONSTRAINT `fk_session_user`
                        FOREIGN KEY (id_user) REFERENCES users (id_user)
                    );
                    """
                    .formatted(CryptoUtils.TOKEN_LENGTH_BASE64);
            connection.prepareStatement(statementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize session data access object!", e);
        }
    }

    public @NotNull Optional<Session> sessionById(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_user, creation_time, expiration_time, token
                    FROM sessions WHERE id_session = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Session.Builder()
                    .id(id)
                    .userId(result.getInt(1))
                    .creationTime(result.getTimestamp(2).toLocalDateTime())
                    .expirationTime(result.getTimestamp(3).toLocalDateTime())
                    .token(result.getString(4))
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find session!", e);
        }
    }


    public @NotNull Optional<Session> sessionByToken(final @NotNull String token) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_session, id_user, creation_time, expiration_time
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
                    .creationTime(result.getTimestamp(3).toLocalDateTime())
                    .expirationTime(result.getTimestamp(4).toLocalDateTime())
                    .token(token)
                    .build());
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find session!", e);
        }
    }

    public boolean existsSessionId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM sessions WHERE id_session = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for session!", e);
        }
    }


    public boolean existsSessionToken(final @NotNull String token) {
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
            throw new RuntimeException("Could search for session!", e);
        }
    }

    public @NotNull Session insertSession(final @NotNull SessionSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO sessions (id_user, creation_time, expiration_time, token)
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
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Session.Builder()
                    .id(result.getInt(1))
                    .userId(skeleton.userId())
                    .creationTime(creation)
                    .expirationTime(expiration)
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
                    UPDATE sessions SET expiration_time = CURRENT_TIMESTAMP WHERE id_session = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                           .setInt(id)
                           .unwrap()
                           .executeUpdate() > 0;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not end session!", e);
        }
    }
}