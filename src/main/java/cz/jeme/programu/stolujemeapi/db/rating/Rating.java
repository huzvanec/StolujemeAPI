package cz.jeme.programu.stolujemeapi.db.rating;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Rating implements Entry {
    private final int id;
    private final int mealId;
    private final int userId;
    @Range(from = 0, to = 10)
    private final int rating;
    private final @NotNull LocalDateTime ratingTime;

    private Rating(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        mealId = Objects.requireNonNull(builder.mealId, "mealId");
        userId = Objects.requireNonNull(builder.userId, "userId");
        rating = Objects.requireNonNull(builder.rating, "rating");
        ratingTime = Objects.requireNonNull(builder.ratingTime, "ratingTime");
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

    public @NotNull LocalDateTime ratingTime() {
        return ratingTime;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final Rating rating1)) return false;

        return id == rating1.id && mealId == rating1.mealId && userId == rating1.userId && rating == rating1.rating && ratingTime.equals(rating1.ratingTime);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + mealId;
        result = 31 * result + userId;
        result = 31 * result + rating;
        result = 31 * result + ratingTime.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "Rating{" +
               "id=" + id +
               ", mealId=" + mealId +
               ", userId=" + userId +
               ", rating=" + rating +
               ", ratingTime=" + ratingTime +
               '}';
    }

    static class Builder implements Entry.Builder<Builder, Rating> {
        private @Nullable Integer id;
        private @Nullable Integer mealId;
        private @Nullable Integer userId;
        @Range(from = 0, to = 10)
        private @Nullable Integer rating;
        private @Nullable LocalDateTime ratingTime;

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

        public @NotNull Builder ratingTime(final @NotNull LocalDateTime ratingTime) {
            this.ratingTime = ratingTime;
            return this;
        }

        @Override
        public @NotNull Rating build() {
            return new Rating(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }
    }
}
