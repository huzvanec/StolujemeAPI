package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import cz.jeme.programu.stolujemeapi.db.StoluStatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class StoluVerificationDao implements VerificationDao {
    private final @NotNull Database database;
    private final @NotNull StatementWrapper wrapper = new StoluStatementWrapper();

    public StoluVerificationDao(final @NotNull Database database) {
        this.database = database;
    }

    @Override
    public void init() {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
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
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize verification data access object!", e);
        }
    }

    @Override
    public @NotNull Optional<Verification> getById(final int id) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT user_id, creation_timestamp, expiration_timestamp, code
                    FROM verifications WHERE verification_id = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluVerification(
                    id,
                    result.getInt(1),
                    result.getTimestamp(2).toLocalDateTime(),
                    result.getTimestamp(3).toLocalDateTime(),
                    result.getString(4)
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find verification by id!", e);
        }
    }


    @Override
    public @NotNull Optional<Verification> getByCode(final @NotNull String code) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT verification_id, user_id, creation_timestamp, expiration_timestamp
                    FROM verifications WHERE code = ?;
                    """;
            ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(code)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new StoluVerification(
                    result.getInt(1),
                    result.getInt(2),
                    result.getTimestamp(3).toLocalDateTime(),
                    result.getTimestamp(4).toLocalDateTime(),
                    code
            ));
        } catch (SQLException e) {
            throw new RuntimeException("Could not find verification by code!", e);
        }
    }

    @Override
    public boolean existsId(final int id) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM verifications WHERE verification_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for verification id!", e);
        }
    }


    @Override
    public boolean existsCode(final @NotNull String code) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    SELECT 1 FROM verifications WHERE code = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(code)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (SQLException e) {
            throw new RuntimeException("Could search for verification code!", e);
        }
    }

    @Override
    public @NotNull Verification insert(final @NotNull VerificationSkeleton skeleton) {
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String statementStr = """
                    INSERT INTO verifications (user_id, creation_timestamp, expiration_timestamp, code)
                    VALUES (?, ?, ?, ?);
                    """;
            LocalDateTime creation = LocalDateTime.now();
            LocalDateTime expiration = creation.plus(skeleton.duration());
            PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.userId())
                    .setTimestamp(Timestamp.valueOf(creation))
                    .setTimestamp(Timestamp.valueOf(expiration))
                    .setString(skeleton.code())
                    .unwrap();
            statement.execute();
            ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Verification id was not returned!");
            return new StoluVerification(
                    result.getInt(1),
                    skeleton.userId(),
                    creation,
                    expiration,
                    skeleton.code()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Could not create verification!", e);
        }
    }


    @Override
    public void verify(final @NotNull Verification verification) {
        if (verification.expired())
            throw new IllegalArgumentException("The verification provided has already expired!");
        try (Connection connection = database.openConnection()) {
            // language=mariadb
            String userStatementStr = """
                    UPDATE users SET verified = TRUE WHERE user_id = ?;
                    """;
            wrapper.wrap(connection.prepareStatement(userStatementStr))
                    .setInt(verification.userId())
                    .unwrap()
                    .execute();

            // language=mariadb
            String cleanupStatementStr = """
                    UPDATE verifications SET expiration_timestamp = CURRENT_TIMESTAMP WHERE user_id = ?;
                    """;
            wrapper.wrap(connection.prepareStatement(cleanupStatementStr))
                    .setInt(verification.userId())
                    .unwrap()
                    .execute();
        } catch (SQLException e) {
            throw new RuntimeException("Could not verify user!", e);
        }
    }
}
