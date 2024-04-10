package cz.jeme.programu.stolujemeapi;

import com.fasterxml.jackson.databind.JsonNode;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public enum Canteen {
    CESKOLIPSKA(
            "0158",
            meal -> !meal.get("nazev").asText().equals(meal.get("druh_popis").asText()),
            type -> switch (type) {
                case "P" -> Meal.Course.SOUP;
                case "D" -> Meal.Course.ADDITION;
                default -> {
                    try {
                        final int ignored = Integer.parseInt(type);
                        yield Meal.Course.MAIN;
                    } catch (final NumberFormatException e) {
                        throw new RuntimeException("Unknown meal type: \"%s\""
                                .formatted(type), e);
                    }
                }
            }
    );

    private final @NotNull String number;
    private final @NotNull Predicate<@NotNull JsonNode> mealValidator;
    private final @NotNull Function<@NotNull String, Meal.@NotNull Course> courseTranslator;

    Canteen(final @NotNull String number,
            final @NotNull Predicate<@NotNull JsonNode> mealValidator,
            final @NotNull Function<@NotNull String, Meal.@NotNull Course> courseTranslator) {
        this.number = number;
        this.mealValidator = mealValidator;
        this.courseTranslator = courseTranslator;
    }

    public @NotNull String number() {
        return number;
    }

    public boolean mealValid(@NotNull final JsonNode meal) {
        return mealValidator.test(meal);
    }

    public @NotNull Meal.Course course(final @NotNull String type) {
        return courseTranslator.apply(type);
    }
}
