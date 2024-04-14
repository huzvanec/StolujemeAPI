package cz.jeme.programu.stolujemeapi.db.rating;

import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public enum RatingDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;
    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String photosStatementStr = """
                    CREATE TABLE IF NOT EXISTS ratings (
                    id_rating MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    id_user MEDIUMINT UNSIGNED NOT NULL,
                    rating TINYINT(10) UNSIGNED NOT NULL,
                    rating_time DATETIME NOT NULL,
                    CONSTRAINT `fk_rating_meal`
                        FOREIGN KEY (id_meal) REFERENCES meals (id_meal),
                    CONSTRAINT `fk_rating_user`
                        FOREIGN KEY (id_user) REFERENCES users (id_user),
                    UNIQUE KEY `uq_meal_user` (id_meal, id_user)
                    );
                    """;
            connection.prepareStatement(photosStatementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize rating data access object!", e);
        }
    }

    public boolean existsRatingMealIdUserId(final int mealId, final int userId) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM ratings WHERE id_meal = ? AND id_user = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(mealId)
                    .setInt(userId)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for rating!", e);
        }
    }

    public @NotNull Optional<Rating> ratingByMealIdUserId(final int mealId, final int userId) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_rating, rating, rating_time
                    FROM ratings WHERE id_meal = ? AND id_user = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(mealId)
                    .setInt(userId)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Rating.Builder()
                    .id(result.getInt(1))
                    .mealId(mealId)
                    .userId(userId)
                    .rating(result.getInt(2))
                    .ratingTime(result.getTimestamp(3).toLocalDateTime())
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find rating!", e);
        }
    }

    public @NotNull Rating rate(final @NotNull RatingSkeleton skeleton) {
        if (existsRatingMealIdUserId(skeleton.mealId(), skeleton.userId())) {
            updateRating(skeleton);
            return ratingByMealIdUserId(skeleton.mealId(), skeleton.userId())
                    .orElseThrow(() -> new RuntimeException("Existing rating could not be found!"));
        } else {
            return insertRating(skeleton);
        }
    }

    private @NotNull Rating insertRating(final @NotNull RatingSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO ratings (id_meal, id_user, rating, rating_time)
                    VALUES (?, ?, ?, ?);
                    """;
            final LocalDateTime ratingTime = LocalDateTime.now();
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setInt(skeleton.userId())
                    .setInt(skeleton.rating())
                    .setTimestamp(Timestamp.valueOf(ratingTime))
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Rating.Builder()
                    .id(result.getInt(1))
                    .mealId(skeleton.mealId())
                    .userId(skeleton.userId())
                    .rating(skeleton.rating())
                    .ratingTime(ratingTime)
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create rating!", e);
        }
    }

    private void updateRating(final @NotNull RatingSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    UPDATE ratings SET rating = ?, rating_time = ? WHERE id_meal = ? AND id_user = ?;
                    """;
            final LocalDateTime ratingTime = LocalDateTime.now();
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr))
                    .setInt(skeleton.rating())
                    .setTimestamp(Timestamp.valueOf(ratingTime))
                    .setInt(skeleton.mealId())
                    .setInt(skeleton.userId())
                    .unwrap();
            if (statement.executeUpdate() < 1)
                throw new RuntimeException("No rating was updated!");
        } catch (final SQLException e) {
            throw new RuntimeException("Could not update rating!", e);
        }
    }
}
