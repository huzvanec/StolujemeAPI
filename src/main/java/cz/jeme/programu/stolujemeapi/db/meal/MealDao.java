package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.sql.ResultWrapper;
import cz.jeme.programu.stolujemeapi.sql.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

public enum MealDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String mealsStatementStr = """
                    CREATE TABLE IF NOT EXISTS meals (
                    id_meal MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    uuid UUID UNIQUE NOT NULL,
                    canteen VARCHAR(30) NOT NULL,
                    course VARCHAR(30) NOT NULL,
                    description VARCHAR(1000) NULL DEFAULT NULL
                    );
                    """;
            connection.prepareStatement(mealsStatementStr).execute();
            // language=mariadb
            final String mealNamesStatementStr = """
                    CREATE TABLE IF NOT EXISTS meal_names (
                    id_meal_name MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    FOREIGN KEY (id_meal) REFERENCES meals (id_meal),
                    name VARCHAR(500) NOT NULL UNIQUE
                    );
                    """;
            connection.prepareStatement(mealNamesStatementStr).execute();
            // language=mariadb
            final String menuStatementStr = """
                    CREATE TABLE IF NOT EXISTS menu (
                    id_menu MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    FOREIGN KEY (id_meal) REFERENCES meals (id_meal),
                    id_meal_name MEDIUMINT UNSIGNED NOT NULL,
                    FOREIGN KEY (id_meal_name) REFERENCES meal_names (id_meal_name),
                    uuid UUID UNIQUE NOT NULL,
                    date DATE NOT NULL,
                    course_number TINYINT NULL DEFAULT NULL,
                    CHECK (course_number >= 1)
                    );
                    """;
            connection.prepareStatement(menuStatementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize meal data access object!", e);
        }
    }

    // MEAL

    public @NotNull Optional<Meal> mealById(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT uuid, canteen, course, description
                    FROM meals WHERE id_meal = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Meal.Builder()
                    .id(id)
                    .uuid(result.getUUID())
                    .canteen(result.getCanteen())
                    .course(result.getCourse())
                    .description(result.getString())
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }

    public @NotNull Optional<Meal> mealByUuid(final @NotNull UUID uuid) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_meal, canteen, course, description
                    FROM meals WHERE uuid = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setUUID(uuid)
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Meal.Builder()
                    .id(result.getInt())
                    .uuid(uuid)
                    .canteen(result.getCanteen())
                    .course(result.getCourse())
                    .description(result.getString())
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }


    public @NotNull Meal insertMeal(final @NotNull MealSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO meals (uuid, canteen, course, description)
                    VALUES (?, ?, ?, ?);
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setUUID(skeleton.uuid())
                    .setCanteen(skeleton.canteen())
                    .setCourse(skeleton.course())
                    .setNullString(skeleton.description())
                    .executeGenerate();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Meal.Builder()
                    .id(result.getInt())
                    .uuid(skeleton.uuid())
                    .course(skeleton.course())
                    .canteen(skeleton.canteen())
                    .description(skeleton.description())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create meal!", e);
        }
    }

    // MENU ENTRY

    public @NotNull List<MenuEntry> menuEntriesByDates(final @NotNull Canteen canteen,
                                                       final @NotNull LocalDate fromDate,
                                                       final @NotNull LocalDate toDate) {
        if (fromDate.isAfter(toDate))
            throw new IllegalArgumentException("From date is after to date!");
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT menu.id_menu, menu.uuid, menu.date, menu.course_number,
                           meal_names.name,
                           meals.id_meal, meals.uuid, meals.canteen,  meals.course, meals.description
                    FROM menu, meal_names, meals
                    WHERE date BETWEEN ? AND ?
                    AND meals.canteen = ?
                    AND menu.id_meal_name = meal_names.id_meal_name
                    AND menu.id_meal = meals.id_meal
                    ORDER BY date;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setLocalDate(fromDate)
                    .setLocalDate(toDate)
                    .setString(canteen.name())
                    .executeQuery();

            final List<MenuEntry> menuEntries = new ArrayList<>();
            while (result.next()) {
                menuEntries.add(new MenuEntry.Builder()
                        .id(result.getInt())
                        .uuid(result.getUUID())
                        .date(result.getLocalDate())
                        .courseNumber(result.getNullInteger())
                        .mealName(result.getString())
                        .meal(new Meal.Builder()
                                .id(result.getInt())
                                .uuid(result.getUUID())
                                .canteen(result.getCanteen())
                                .course(result.getCourse())
                                .description(result.getString())
                                .build()
                        ).build()
                );
            }
            return menuEntries;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find menu entry!", e);
        }
    }

    public void updateMenuDay(final @NotNull LocalDate date, final @NotNull Collection<MenuEntrySkeleton> entries) {
        try (final Connection connection = database.connection()) {
            connection.setAutoCommit(false);
            final StatementWrapper wrapper = StatementWrapper.wrapper();
            try {
                final Set<Integer> deletions = new HashSet<>();
                // language=mariadb
                final String deletionsStatementStr = """
                        SELECT id_menu FROM menu WHERE date = ?;
                        """;
                final ResultWrapper deletionsResult = wrapper.wrap(connection.prepareStatement(deletionsStatementStr))
                        .setLocalDate(date)
                        .executeQuery();

                while (deletionsResult.next()) deletions.add(deletionsResult.getInt());

                final List<MenuEntrySkeleton> additions = new ArrayList<>();
                for (final MenuEntrySkeleton skeleton : entries) {
                    // language=mariadb
                    final String existenceStatementStr = """
                            SELECT id_menu FROM menu
                            WHERE date = ?
                            AND id_meal = ?
                            AND id_meal_name = ?
                            AND IFNULL(course_number, -1) = IFNULL(?, -1); -- null-wise equals
                            """;
                    final ResultWrapper existenceResult = wrapper.wrap(connection.prepareStatement(existenceStatementStr))
                            .setLocalDate(date)
                            .setInt(skeleton.mealId())
                            .setInt(skeleton.mealNameId())
                            .setNullInteger(skeleton.courseNumber())
                            .executeQuery();

                    if (existenceResult.next()) {
                        deletions.remove(existenceResult.getInt());
                    } else {
                        additions.add(skeleton);
                    }
                }

                // language=mariadb
                final String deleteStatementStr = """
                        DELETE FROM menu WHERE id_menu = ?;
                        """;
                for (final int deletion : deletions)
                    wrapper.wrap(connection.prepareStatement(deleteStatementStr))
                            .setInt(deletion)
                            .execute();

                // language=mariadb
                final String insertStatementStr = """
                        INSERT INTO menu (id_meal, id_meal_name, uuid, date, course_number)
                        VALUES (?, ?, ?, ?, ?);
                        """;
                for (final MenuEntrySkeleton skeleton : additions) {
                    wrapper.wrap(connection.prepareStatement(insertStatementStr))
                            .setInt(skeleton.mealId())
                            .setInt(skeleton.mealNameId())
                            .setUUID(skeleton.uuid())
                            .setLocalDate(date)
                            .setNullInteger(skeleton.courseNumber())
                            .execute();
                }
                connection.commit();
            } catch (final SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Could not update menu entries!", e);
        }
    }

    public @NotNull Optional<MenuEntry> menuEntryByUuid(final @NotNull UUID uuid) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT menu.id_menu, menu.date, menu.course_number,
                           meal_names.name,
                           meals.id_meal, meals.uuid, meals.canteen,  meals.course, meals.description
                    FROM menu, meal_names, meals
                    WHERE menu.uuid = ?
                    AND menu.id_meal = meals.id_meal
                    AND menu.id_meal = meal_names.id_meal;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(uuid.toString())
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new MenuEntry.Builder()
                    .id(result.getInt())
                    .uuid(uuid)
                    .date(result.getLocalDate())
                    .courseNumber(result.getNullInteger())
                    .mealName(result.getString())
                    .meal(new Meal.Builder()
                            .id(result.getInt())
                            .uuid(result.getUUID())
                            .canteen(result.getCanteen())
                            .course(result.getCourse())
                            .description(result.getString())
                            .build())
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }

    // MEAL NAME

    public boolean existsMealName(final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM meal_names WHERE name = ?;
                    """;
            return StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(name)
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for meal!", e);
        }
    }

    public @NotNull Optional<MealName> mealNameByName(final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_meal_name, id_meal
                    FROM meal_names WHERE name = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setString(name)
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new MealName(
                    result.getInt(),
                    result.getInt(),
                    name
            ));
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }

    public @NotNull List<MealName> mealNamesByMealId(final int mealId) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT meal_names.id_meal_name, meal_names.name
                    FROM meal_names, menu WHERE
                    meal_names.id_meal = ? AND
                    meal_names.id_meal_name = menu.id_meal_name
                    ORDER BY menu.date DESC;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(mealId)
                    .executeQuery();

            final List<MealName> names = new ArrayList<>();
            while (result.next()) {
                names.add(new MealName.Builder()
                        .mealId(mealId)
                        .id(result.getInt())
                        .name(result.getString())
                        .build());
            }
            return names;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }

    public @NotNull MealName insertMealName(final @NotNull MealNameSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO meal_names (id_meal, name)
                    VALUES (?, ?);
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setString(skeleton.name())
                    .executeGenerate();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new MealName(
                    result.getInt(),
                    skeleton.mealId(),
                    skeleton.name()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create meal name!", e);
        }
    }
}