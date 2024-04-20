package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.Canteen;
import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

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
                    id_meal MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    uuid UUID UNIQUE NOT NULL,
                    canteen VARCHAR(25) NOT NULL,
                    course VARCHAR(25) NOT NULL
                    );
                    """;
            connection.prepareStatement(mealsStatementStr).execute();
            // language=mariadb
            final String mealNamesStatementStr = """
                    CREATE TABLE IF NOT EXISTS meal_names (
                    id_meal_name MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    name VARCHAR(250) NOT NULL UNIQUE,
                    CONSTRAINT `fk_meal_name_meal`
                        FOREIGN KEY (id_meal) REFERENCES meals (id_meal)
                    );
                    """;
            connection.prepareStatement(mealNamesStatementStr).execute();
            // language=mariadb
            final String menuStatementStr = """
                    CREATE TABLE IF NOT EXISTS menu (
                    id_menu MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    id_meal_name MEDIUMINT UNSIGNED NOT NULL,
                    date DATE NOT NULL,
                    course_number TINYINT,
                    CONSTRAINT `fk_menu_meal`
                        FOREIGN KEY (id_meal) REFERENCES meals (id_meal)
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
                    SELECT uuid, canteen, course
                    FROM meals WHERE id_meal = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Meal.Builder()
                    .id(id)
                    .uuid(UUID.fromString(result.getString(1)))
                    .canteen(Canteen.valueOf(result.getString(2)))
                    .course(Meal.Course.valueOf(result.getString(3)))
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
                    SELECT id_meal, canteen, course
                    FROM meals WHERE uuid = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(uuid.toString())
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Meal.Builder()
                    .id(result.getInt(1))
                    .uuid(uuid)
                    .canteen(Canteen.valueOf(result.getString(2)))
                    .course(Meal.Course.valueOf(result.getString(3)))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }

    public boolean existsMealId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM meals WHERE id_meal = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for meal!", e);
        }
    }


    public @NotNull Meal insertMeal(final @NotNull MealSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO meals (uuid, canteen, course)
                    VALUES (?, ?, ?);
                    """;
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setString(skeleton.uuid().toString())
                    .setString(skeleton.canteen().toString())
                    .setString(skeleton.course().toString())
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Meal.Builder()
                    .id(result.getInt(1))
                    .uuid(skeleton.uuid())
                    .course(skeleton.course())
                    .canteen(skeleton.canteen())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create meal!", e);
        }
    }

    public boolean existsMealUuid(final @NotNull UUID uuid) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM meals WHERE uuid = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(uuid.toString())
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for meal!", e);
        }
    }

    // MENU ENTRY

    public @NotNull Optional<MenuEntry> menuEntryById(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_meal, date, course_number
                    FROM menu WHERE id_menu = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new MenuEntry.Builder()
                    .id(id)
                    .mealId(result.getInt(1))
                    .date(result.getDate(2).toLocalDate())
                    .courseNumber(result.getObject(3) == null
                            ? null
                            : result.getInt(3))
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find menu entry!", e);
        }
    }

    public @NotNull List<MenuEntry> menuEntriesByDates(final @NotNull LocalDate fromDate,
                                                       final @NotNull LocalDate toDate) {
        if (fromDate.isAfter(toDate))
            throw new IllegalArgumentException("From date is after to date!");
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_menu, id_meal, date, course_number
                    FROM menu WHERE date BETWEEN ? AND ?
                    ORDER BY date;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setDate(Date.valueOf(fromDate))
                    .setDate(Date.valueOf(toDate))
                    .unwrap()
                    .executeQuery();

            final List<MenuEntry> menuEntries = new ArrayList<>();
            while (result.next()) {
                menuEntries.add(new MenuEntry.Builder()
                        .id(result.getInt(1))
                        .mealId(result.getInt(2))
                        .date(result.getDate(3).toLocalDate())
                        .courseNumber(result.getObject(4) == null
                                ? null
                                : result.getInt(4))
                        .build()
                );
            }
            return menuEntries;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find menu entry!", e);
        }
    }


    public boolean existsMenuEntryId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM menu WHERE id_menu = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery()
                    .next();
        } catch (final SQLException e) {
            throw new RuntimeException("Could search for menu entry!", e);
        }
    }

    public @NotNull MenuEntry insertMenuEntry(final @NotNull MenuEntrySkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO menu (id_meal, date, course_number)
                    VALUES (?, ?, ?);
                    """;
            final PreparedStatement statement = wrapper.wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setDate(Date.valueOf(skeleton.date()))
                    .setInteger(skeleton.courseNumber(), Types.NULL)
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new MenuEntry.Builder()
                    .id(result.getInt(1))
                    .mealId(skeleton.mealId())
                    .date(skeleton.date())
                    .courseNumber(skeleton.courseNumber())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create menu entry!", e);
        }
    }

    public void updateMenuDay(final @NotNull LocalDate date, final @NotNull Collection<MenuEntrySkeleton> entries) {
        try (final Connection connection = database.connection()) {
            connection.setAutoCommit(false);
            try {
                // language=mariadb
                final String deleteStatementStr = """
                        DELETE FROM menu WHERE date = ?;
                        """;
                wrapper.wrap(connection.prepareStatement(deleteStatementStr))
                        .setDate(Date.valueOf(date))
                        .unwrap()
                        .execute();

                // language=mariadb
                final String insertStatementStr = """
                        INSERT INTO menu (id_meal, date, course_number) VALUES (?, ?, ?);
                        """;
                for (final MenuEntrySkeleton skeleton : entries) {
                    wrapper.wrap(connection.prepareStatement(insertStatementStr))
                            .setInt(skeleton.mealId())
                            .setDate(Date.valueOf(date))
                            .setInteger(skeleton.courseNumber(), Types.NULL)
                            .unwrap()
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

    // MEAL NAME

    public boolean existsMealName(final @NotNull String name) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT 1 FROM meal_names WHERE name = ?;
                    """;
            return wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(name)
                    .unwrap()
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
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(name)
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new MealName(
                    result.getInt(1),
                    result.getInt(2),
                    name
            ));
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal!", e);
        }
    }

    public @NotNull List<MealName> mealNamesByMealId(final int id) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_meal_name, name
                    FROM meal_names WHERE id_meal = ?;
                    """;
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(id)
                    .unwrap()
                    .executeQuery();
            final List<MealName> names = new ArrayList<>();
            while (result.next()) {
                names.add(new MealName(
                        result.getInt(1),
                        id,
                        result.getString(2)
                ));
            }
            return names;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find meal names!", e);
        }
    }

    public @NotNull MealName insertMealName(final @NotNull MealNameSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO meal_names (id_meal, name)
                    VALUES (?, ?);
                    """;
            wrapper.wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setString(skeleton.name());
            final PreparedStatement statement = wrapper.unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new MealName(
                    result.getInt(1),
                    skeleton.mealId(),
                    skeleton.name()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create meal name!", e);
        }
    }
}
