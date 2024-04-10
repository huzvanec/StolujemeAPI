package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;

public record MealName(
        int id,
        int mealId,
        @NotNull String name
) implements Entry {
}
