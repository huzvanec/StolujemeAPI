package cz.jeme.programu.stolujemeapi.db.rating;

import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.sql.ResultWrapper;
import cz.jeme.programu.stolujemeapi.sql.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public enum RatingDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String ratingsStatementStr = """
                    CREATE TABLE IF NOT EXISTS ratings (
                    id_rating INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    FOREIGN KEY (id_meal) REFERENCES meals (id_meal),
                    id_menu MEDIUMINT UNSIGNED NOT NULL,
                    FOREIGN KEY (id_menu) REFERENCES menu (id_menu),
                    id_user MEDIUMINT UNSIGNED NOT NULL,
                    FOREIGN KEY (id_user) REFERENCES users (id_user),
                    UNIQUE KEY (id_menu, id_user),
                    rating TINYINT UNSIGNED NOT NULL,
                    CHECK (rating BETWEEN 1 AND 10),
                    rating_time DATETIME NOT NULL
                    );
                    """;
            connection.prepareStatement(ratingsStatementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize rating data access object!", e);
        }
    }

    private boolean existsRatingUserIdMenuId(final int userId,
                                             final int menuId) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM ratings
                    WHERE id_user = ?
                    AND id_menu = ?;
                    """;

            return StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(userId)
                    .setInt(menuId)
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not search for rating!", e);
        }
    }

    public void rate(final @NotNull RatingSkeleton skeleton) {
        if (existsRatingUserIdMenuId(skeleton.userId(), skeleton.menuId())) {
            updateRating(skeleton);
        } else {
            insertRating(skeleton);
        }
    }

//    public @NotNull Rating unrate(final int userId, final @NotNull int menuId) {
//
//    }

    private @NotNull Rating insertRating(final @NotNull RatingSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO ratings (id_meal, id_menu, id_user, rating, rating_time)
                    VALUES (?, ?, ?, ?, ?);
                    """;
            final LocalDateTime ratingTime = LocalDateTime.now();
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setInt(skeleton.menuId())
                    .setInt(skeleton.userId())
                    .setInt(skeleton.rating())
                    .setLocalDateTime(ratingTime)
                    .executeGenerate();

            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Rating.Builder()
                    .id(result.getInt())
                    .mealId(skeleton.mealId())
                    .menuId(skeleton.menuId())
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
                    UPDATE ratings SET rating = ?, rating_time = ?
                    WHERE id_menu = ? AND id_user = ?;
                    """;
            final LocalDateTime ratingTime = LocalDateTime.now();
            final int rows = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(skeleton.rating())
                    .setLocalDateTime(ratingTime)
                    .setInt(skeleton.menuId())
                    .setInt(skeleton.userId())
                    .executeUpdate();
            if (rows < 1) throw new RuntimeException("No rating was updated!");
        } catch (final SQLException e) {
            throw new RuntimeException("Could not update rating!", e);
        }
    }

    public @NotNull Map<UUID, Double> ratingsByUserId(final int userId) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT meals.uuid, AVG(ratings.rating)
                    FROM ratings, meals
                    WHERE ratings.id_user = ?
                    AND ratings.id_meal = meals.id_meal
                    GROUP BY meals.uuid;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(userId)
                    .executeQuery();
            final Map<UUID, Double> ratings = new HashMap<>();
            while (result.next()) {
                ratings.put(
                        result.getUUID(),
                        result.getDouble()
                );
            }
            return ratings;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find ratings!", e);
        }
    }

    public enum RatingRequestType {
        USER("="),
        GLOBAL("<>");

        private final @NotNull String operator;

        RatingRequestType(final @NotNull String operator) {
            this.operator = operator;
        }

        public @NotNull String operator() {
            return operator;
        }
    }

    public @NotNull Map<Integer, Double> ratingsByDates(final @NotNull LocalDate fromDate,
                                                        final @NotNull LocalDate toDate,
                                                        final @NotNull RatingRequestType type,
                                                        final int userId) {
        if (fromDate.isAfter(toDate))
            throw new IllegalArgumentException("From date is after to date!");
        try (final Connection connection = database.connection()) {
            // \language=mariadb
            final String statementStr = """
                    SELECT menu.id_meal, AVG(ratings.rating)
                    FROM menu, ratings
                    WHERE menu.date BETWEEN ? AND ?
                    AND ratings.id_user %s ?
                    AND menu.id_meal = ratings.id_meal
                    GROUP BY menu.id_meal
                    """
                    // language=reset
                    .formatted(type.operator());
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setLocalDate(fromDate)
                    .setLocalDate(toDate)
                    .setInt(userId)
                    .executeQuery();

            final Map<Integer, Double> ratings = new HashMap<>();

            while (result.next()) {
                ratings.put(
                        result.getInt(),
                        result.getDouble()
                );
            }
            return ratings;
        } catch (
                final SQLException e) {
            throw new RuntimeException("Could not find ratings!", e);
        }
    }
}
