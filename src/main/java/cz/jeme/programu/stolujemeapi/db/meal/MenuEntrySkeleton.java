package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record MenuEntrySkeleton(
        int mealId,
        int mealNameId,
        @NotNull UUID uuid,
        @NotNull LocalDate date,
        @Nullable Integer courseNumber
) implements Skeleton {
    private MenuEntrySkeleton(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.mealNameId, "mealNameId"),
                builder.uuid,
                Objects.requireNonNull(builder.date, "date"),
                builder.courseNumber
        );
    }

    public static final class Builder implements Skeleton.Builder<Builder, MenuEntrySkeleton> {
        private @Nullable Integer mealId;
        private @Nullable Integer mealNameId;
        private @NotNull UUID uuid = UUID.randomUUID();
        private @Nullable LocalDate date;
        private @Nullable Integer courseNumber;

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
            return this;
        }

        public @NotNull Builder mealNameId(final int mealNameId) {
            this.mealNameId = mealNameId;
            return this;
        }

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder date(final @NotNull LocalDate date) {
            this.date = date;
            return this;
        }

        public @NotNull Builder courseNumber(final @Nullable Integer courseNumber) {
            this.courseNumber = courseNumber;
            return this;
        }

        @Override
        public @NotNull MenuEntrySkeleton build() {
            return new MenuEntrySkeleton(this);
        }
    }
}
