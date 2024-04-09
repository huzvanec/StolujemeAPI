package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class MealSkeleton implements Skeleton {
    private final @NotNull UUID uuid;
    private final @NotNull Meal.Course course;

    private MealSkeleton(final @NotNull Builder builder) {
        this.uuid = builder.uuid;
        this.course = Objects.requireNonNull(builder.course, "course");
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull Meal.Course course() {
        return course;
    }

    public static final class Builder implements Skeleton.Builder<Builder, MealSkeleton> {
        private @NotNull UUID uuid = UUID.randomUUID();
        private @Nullable Meal.Course course;

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder course(final @NotNull Meal.Course course) {
            this.course = course;
            return this;
        }


        @Override
        public @NotNull MealSkeleton build() {
            return new MealSkeleton(this);
        }
    }
}
