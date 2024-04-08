package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public enum VerificationDao implements Dao<Verification, VerificationSkeleton> {
    INSTANCE;

    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();
    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    CREATE TABLE IF NOT EXISTS verifications (
                    verification_id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    user_id MEDIUMINT UNSIGNED NOT NULL,
                    creation_timestamp DATETIME NOT NULL,
                    expiration_timestamp DATETIME NOT NULL,
                    code VARCHAR(%d) NOT NULL UNIQUE,
                    CONSTRAINT `fk_verification_user`
                        FOREIGN KEY (user_id) REFERENCES users (user_id)
                    );
                    """
                    .formatted(CryptoUtils.VERIFICATION_LENGTH_BASE64);
            connection.prepareStatement(statementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize verification data access object!", e);
        }
    }

    @Override
    public @NotNull Optional<Verification> byId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT user_id, creation_timestamp, expiration_timestamp, code
                    FROM verifications WHERE verification_id = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Verification.Builder()
                    .id(id)
                    .userId(result.getInt(1))
                    .creation(result.getTimestamp(2).toLocalDateTime())
                    .expiration(result.getTimestamp(3).toLocalDateTime())
                    .code(result.getString(4))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find verification by id!", e);
        }
    }


    public @NotNull Optional<Verification> byCode(final @NotNull String code) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT verification_id, user_id, creation_timestamp, expiration_timestamp
                    FROM verifications WHERE code = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(code)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();

            return Optional.of(new Verification.Builder()
                    .id(result.getInt(1))
                    .userId(result.getInt(2))
                    .creation(result.getTimestamp(3).toLocalDateTime())
                    .expiration(result.getTimestamp(4).toLocalDateTime())
                    .code(code)
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find verification by code!", e);
        }
    }

    @Override
    public boolean existsId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM verifications WHERE verification_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for verification id!", e);
        }
    }


    public boolean existsCode(final @NotNull String code) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM verifications WHERE code = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(code)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for verification code!", e);
        }
    }

    @Override
    public @NotNull Verification insert(final @NotNull VerificationSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO verifications (user_id, creation_timestamp, expiration_timestamp, code)
                    VALUES (?, ?, ?, ?);
                    """;
            final LocalDateTime creation = LocalDateTime.now();
            final LocalDateTime expiration = creation.plus(skeleton.duration());
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.userId())
                    .setTimestamp(Timestamp.valueOf(creation))
                    .setTimestamp(Timestamp.valueOf(expiration))
                    .setString(skeleton.code())
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Verification id was not returned!");
            return new Verification.Builder()
                    .id(result.getInt(1))
                    .userId(skeleton.userId())
                    .creation(creation)
                    .expiration(expiration)
                    .code(skeleton.code())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create verification!", e);
        }
    }


    public void verify(final @NotNull Verification verification) {
        if (verification.expired())
            throw new IllegalArgumentException("The verification provided has already expired!");
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String userStatementStr = """
                    UPDATE users SET verified = TRUE WHERE user_id = ?;
                    """;
            wrapper.wrap(connection.prepareStatement(userStatementStr))
                    .setInt(verification.userId())
                    .unwrap()
                    .execute();

            // language=mariadb
            final String cleanupStatementStr = """
                    UPDATE verifications SET expiration_timestamp = CURRENT_TIMESTAMP WHERE user_id = ?;
                    """;
            wrapper.wrap(connection.prepareStatement(cleanupStatementStr))
                    .setInt(verification.userId())
                    .unwrap()
                    .execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not verify user!", e);
        }
    }
}
