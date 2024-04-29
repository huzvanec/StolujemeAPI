package cz.jeme.programu.stolujemeapi.db.rating;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.LocalDateTime;
import java.util.Objects;

public record Rating(
        int id,
        int mealId,
        int menuId,
        int userId,
        @Range(from = 1, to = 10) int rating,
        @NotNull LocalDateTime ratingTime
) implements Entry {
    private Rating(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.menuId, "menuId"),
                Objects.requireNonNull(builder.userId, "userId"),
                Objects.requireNonNull(builder.rating, "rating"),
                Objects.requireNonNull(builder.ratingTime, "ratingTime")
        );
    }

    @ApiStatus.Internal
    public static class Builder implements Entry.Builder<Builder, Rating> {
        private @Nullable Integer id;
        private @Nullable Integer mealId;
        private @Nullable Integer menuId;
        private @Nullable Integer userId;
        @Range(from = 1, to = 10)
        private @Nullable Integer rating;
        private @Nullable LocalDateTime ratingTime;

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

        public @NotNull Builder ratingTime(final @Nullable LocalDateTime ratingTime) {
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
