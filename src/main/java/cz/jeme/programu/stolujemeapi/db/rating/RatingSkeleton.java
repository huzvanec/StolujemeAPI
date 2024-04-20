package cz.jeme.programu.stolujemeapi.db.rating;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public final class RatingSkeleton implements Skeleton {
    private final int mealId;
    private final int userId;
    @Range(from = 0, to = 10)
    private final int rating;

    private RatingSkeleton(final @NotNull Builder builder) {
        mealId = Objects.requireNonNull(builder.mealId, "mealId");
        userId = Objects.requireNonNull(builder.userId, "userId");
        rating = Objects.requireNonNull(builder.rating, "rating");
    }

    public int mealId() {
        return mealId;
    }

    public int userId() {
        return userId;
    }

    public @Range(from = 0, to = 10) int rating() {
        return rating;
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final RatingSkeleton that)) return false;

        return mealId == that.mealId && userId == that.userId && rating == that.rating;
    }

    @Override
    public int hashCode() {
        int result = mealId;
        result = 31 * result + userId;
        result = 31 * result + rating;
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "RatingSkeleton{" +
               "mealId=" + mealId +
               ", userId=" + userId +
               ", rating=" + rating +
               '}';
    }

    public static class Builder implements Skeleton.Builder<Builder, RatingSkeleton> {
        private @Nullable Integer mealId;
        private @Nullable Integer userId;
        @Range(from = 0, to = 10)
        private @Nullable Integer rating;

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
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
