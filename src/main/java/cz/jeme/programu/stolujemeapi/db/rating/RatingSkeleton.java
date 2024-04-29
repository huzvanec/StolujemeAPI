package cz.jeme.programu.stolujemeapi.db.rating;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public record RatingSkeleton(
        int mealId,
        int menuId,
        int userId,
        @Range(from = 1, to = 10) int rating
) implements Skeleton {
    private RatingSkeleton(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.menuId, "menuId"),
                Objects.requireNonNull(builder.userId, "userId"),
                Objects.requireNonNull(builder.rating, "rating")
        );
    }

    public static class Builder implements Skeleton.Builder<Builder, RatingSkeleton> {
        private @Nullable Integer mealId;
        private @Nullable Integer menuId;
        private @Nullable Integer userId;
        @Range(from = 1, to = 10)
        private @Nullable Integer rating;

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
            return this;
        }

        public @NotNull Builder menuId(final int menuId) {
            this.menuId = menuId;
            return this;
        }

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder rating(final int rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public @NotNull RatingSkeleton build() {
            return new RatingSkeleton(this);
        }
    }
}
