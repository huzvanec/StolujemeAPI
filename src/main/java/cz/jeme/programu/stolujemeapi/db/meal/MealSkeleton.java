package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record MealSkeleton(
        @NotNull UUID uuid,
        @NotNull Meal.Course course,
        @NotNull Canteen canteen,
        @Nullable String description
) implements Skeleton {
    private MealSkeleton(final @NotNull Builder builder) {
        this(
                builder.uuid,
                Objects.requireNonNull(builder.course, "course"),
                Objects.requireNonNull(builder.canteen, "canteen"),
                builder.description
        );
    }

    public static final class Builder implements Skeleton.Builder<Builder, MealSkeleton> {
        private @NotNull UUID uuid = UUID.randomUUID();
        private @Nullable Meal.Course course;
        private @Nullable Canteen canteen;
        private @Nullable String description;

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder course(final @NotNull Meal.Course course) {
            this.course = course;
            return this;
        }

        public @NotNull Builder canteen(final @NotNull Canteen canteen) {
            this.canteen = canteen;
            return this;
        }

        public @NotNull Builder description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        @Override
        public @NotNull MealSkeleton build() {
            return new MealSkeleton(this);
        }
    }
}
