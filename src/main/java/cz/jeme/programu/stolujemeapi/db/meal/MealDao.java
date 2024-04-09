package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public enum MealDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;
    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String mealsStatementStr = """
                    CREATE TABLE IF NOT EXISTS meals (
                    meal_id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    meal_uuid UUID UNIQUE NOT NULL,
                    course VARCHAR(%d) NOT NULL
                    );
                    """
                    // language=reset
                    .formatted(Arrays.stream(Meal.Course.values())
                            .mapToInt(course -> course.toString().length())
                            .max()
                            .orElseThrow(() -> new RuntimeException("No courses exist!"))
                    );
            connection.prepareStatement(mealsStatementStr).execute();
            // language=mariadb
            final String menuStatementStr = """
                    CREATE TABLE IF NOT EXISTS menu (
                    id MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    date DATE NOT NULL,
                    role VARCHAR(%d) NOT NULL
                    );
                    """
                    // language=reset
                    .formatted(Arrays.stream(Meal.Role.values())
                            .mapToInt(role -> role.toString().length())
                            .max()
                            .orElseThrow(() -> new RuntimeException("No roles exist!"))
                    );
            connection.prepareStatement(menuStatementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize meal data access object!", e);
        }
    }

    public @NotNull Optional<Meal> mealById(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT meal_uuid, course
                    FROM meals WHERE meal_id = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Meal.Builder()
                    .id(id)
                    .uuid(UUID.fromString(result.getString(1)))
                    .course(Meal.Course.valueOf(result.getString(2)))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal by id!", e);
        }
    }

    public @NotNull Optional<Meal> mealByUuid(final @NotNull UUID uuid) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT meal_id, course
                    FROM meals WHERE meal_uuid = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(uuid.toString())
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Meal.Builder()
                    .id(result.getInt(1))
                    .uuid(uuid)
                    .course(Meal.Course.valueOf(result.getString(2)))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal by id!", e);
        }
    }

    public boolean existsMealId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM meals WHERE meal_id = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for meal uuid!", e);
        }
    }

    public boolean existsMealUuid(final @NotNull UUID uuid) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM meals WHERE meal_uuid = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(uuid.toString())
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for meal uuid!", e);
        }
    }

    public @NotNull Meal insertMeal(final @NotNull MealSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO meals (meal_uuid, course)
                    VALUES (?, ?);
                    """;
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setString(skeleton.uuid().toString())
                    .setString(skeleton.course().toString())
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Meal id was not returned!");
            return new Meal.Builder()
                    .id(result.getInt(1))
                    .uuid(skeleton.uuid())
                    .course(skeleton.course())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create meal!", e);
        }
    }
}
