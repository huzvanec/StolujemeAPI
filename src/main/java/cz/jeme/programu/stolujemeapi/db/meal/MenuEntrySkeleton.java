package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;

public class MenuEntrySkeleton implements Skeleton {
    private final int mealId;
    private final @NotNull LocalDate date;
    private final @Nullable Integer courseNumber;

    private MenuEntrySkeleton(final @NotNull Builder builder) {
        this.mealId = Objects.requireNonNull(builder.mealId, "mealId");
        this.date = Objects.requireNonNull(builder.date, "date");
        this.courseNumber = builder.courseNumber;
    }

    public int mealId() {
        return mealId;
    }

    public @NotNull LocalDate date() {
        return date;
    }

    public boolean hasCourseNumber() {
        return courseNumber != null;
    }

    public @Nullable Integer courseNumber() {
        return courseNumber;
    }

    @Override
    public @NotNull String toString() {
        return "MenuEntrySkeleton{" +
               "mealId=" + mealId +
               ", date=" + date +
               ", courseNumber=" + courseNumber +
               '}';
    }

    public static final class Builder implements Skeleton.Builder<Builder, MenuEntrySkeleton> {
        private @Nullable Integer mealId;
        private @Nullable LocalDate date;
        private @Nullable Integer courseNumber;

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
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
