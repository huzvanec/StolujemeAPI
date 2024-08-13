package cz.jeme.programu.stolujemeapi.canteen;

import com.fasterxml.jackson.databind.JsonNode;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class Ceskolipska extends Canteen {
    private final @NotNull Set<String> fakeMeals = Set.of(
            "Oběd 1",
            "Oběd 2",
            "Oběd 3",
            "Státní svátek"
    );

    // reflected
    private Ceskolipska() {
        super("CESKOLIPSKA", "0158");
    }

    @Override
    public boolean emailValid(final @NotNull String email) {
        return email.endsWith("@email.cz"); // TODO
    }

    @Override
    public boolean mealValid(final @NotNull JsonNode meal) {
        return !fakeMeals.contains(meal.get("nazev").asText());
    }

    @Override
    public @NotNull Meal.Course translateCourse(final @NotNull String mealType) {
        return switch (mealType) {
            case "P" -> Meal.Course.SOUP;
            case "D" -> Meal.Course.ADDITION;
            default -> {
                try { // validate that it's a number
                    Integer.parseInt(mealType);
                    yield Meal.Course.MAIN;
                } catch (final NumberFormatException e) {
                    throw new RuntimeException("Unknown meal type: " + mealType, e);
                }
            }
        };
    }
}