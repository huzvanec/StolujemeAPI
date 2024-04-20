package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public enum VerificationDao implements Dao {
    INSTANCE;

    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();
    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    CREATE TABLE IF NOT EXISTS verifications (
                    id_verification MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_user MEDIUMINT UNSIGNED NOT NULL,
                    creation_time DATETIME NOT NULL,
                    expiration_time DATETIME NOT NULL,
                    code VARCHAR(%d) NOT NULL UNIQUE,
                    CONSTRAINT `fk_verification_user`
                        FOREIGN KEY (id_user) REFERENCES users (id_user)
                    );
                    """
                    .formatted(CryptoUtils.VERIFICATION_LENGTH_BASE64);
            connection.prepareStatement(statementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize verification data access object!", e);
        }
    }

    public @NotNull Optional<Verification> verificationById(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_user, creation_time, expiration_time, code
                    FROM verifications WHERE id_verification = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Verification.Builder()
                    .id(id)
                    .userId(result.getInt(1))
                    .creationTime(result.getTimestamp(2).toLocalDateTime())
                    .expirationTime(result.getTimestamp(3).toLocalDateTime())
                    .code(result.getString(4))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find verification!", e);
        }
    }


    public @NotNull Optional<Verification> verificationByCode(final @NotNull String code) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_verification, id_user, creation_time, expiration_time
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
                    .creationTime(result.getTimestamp(3).toLocalDateTime())
                    .expirationTime(result.getTimestamp(4).toLocalDateTime())
                    .code(code)
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find verification!", e);
        }
    }

    public boolean existsVerificationId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM verifications WHERE id_verification = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for verification!", e);
        }
    }


    public boolean existsVerificationCode(final @NotNull String code) {
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
            throw new RuntimeException("Could search for verification!", e);
        }
    }

    public @NotNull Verification insertVerification(final @NotNull VerificationSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO verifications (id_user, creation_time, expiration_time, code)
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
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Verification.Builder()
                    .id(result.getInt(1))
                    .userId(skeleton.userId())
                    .creationTime(creation)
                    .expirationTime(expiration)
                    .code(skeleton.code())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create verification!", e);
        }
    }


    public boolean verify(final @NotNull Verification verification) {
        if (verification.expired())
            throw new IllegalArgumentException("The verification provided has already expired!");
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String userStatementStr = """
                    UPDATE users SET verified = TRUE WHERE id_user = ?;
                    """;
            if (wrapper.wrap(connection.prepareStatement(userStatementStr))
                        .setInt(verification.userId())
                        .unwrap()
                        .executeUpdate() <= 0)
                return false;
            // language=mariadb
            final String cleanupStatementStr = """
                    UPDATE verifications SET expiration_time = CURRENT_TIMESTAMP WHERE id_user = ?;
                    """;
            wrapper.wrap(connection.prepareStatement(cleanupStatementStr))
                    .setInt(verification.userId())
                    .unwrap()
                    .execute();
            return true;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not verify user!", e);
        }
    }
}
