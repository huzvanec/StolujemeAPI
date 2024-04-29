package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record MealNameSkeleton(
        int mealId,
        @NotNull String name
) implements Skeleton {
    private MealNameSkeleton(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.name, "name")
        );
    }

    public static final class Builder implements Skeleton.Builder<Builder, MealNameSkeleton> {
        private @Nullable Integer mealId;
        private @Nullable String name;

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
            return this;
        }

        public @NotNull Builder name(final @NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public @NotNull MealNameSkeleton build() {
            return new MealNameSkeleton(this);
        }
    }
}
