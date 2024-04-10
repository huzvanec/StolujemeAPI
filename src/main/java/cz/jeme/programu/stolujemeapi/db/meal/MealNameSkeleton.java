package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;

public record MealNameSkeleton(
        int mealId,
        @NotNull String name
) implements Skeleton {
}
