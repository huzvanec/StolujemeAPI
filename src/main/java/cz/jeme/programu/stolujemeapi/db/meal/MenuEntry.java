package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;

public class MenuEntry implements Entry {
    private final int id;
    private final int mealId;
    private final @NotNull LocalDate date;
    private final @Nullable Integer courseNumber;

    private MenuEntry(final @NotNull Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.mealId = Objects.requireNonNull(builder.mealId, "mealId");
        this.date = Objects.requireNonNull(builder.date, "date");
        this.courseNumber = builder.courseNumber;
    }

    public boolean hasCourseNumber() {
        return courseNumber != null;
    }

    public @Nullable Integer courseNumber() {
        return courseNumber;
    }

    public @NotNull LocalDate date() {
        return date;
    }

    public int mealId() {
        return mealId;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public final boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final MenuEntry menuEntry)) return false;

        return id == menuEntry.id && mealId == menuEntry.mealId && date.equals(menuEntry.date) && Objects.equals(courseNumber, menuEntry.courseNumber);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + mealId;
        result = 31 * result + date.hashCode();
        result = 31 * result + Objects.hashCode(courseNumber);
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "MenuEntry{" +
               "id=" + id +
               ", mealId=" + mealId +
               ", date=" + date +
               ", courseNumber=" + courseNumber +
               '}';
    }

    static final class Builder implements Entry.Builder<Builder, MenuEntry> {
        private @Nullable Integer id;
        private @Nullable Integer mealId;
        private @Nullable LocalDate date;
        private @Nullable Integer courseNumber;


        @Override

        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

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
        public @NotNull MenuEntry build() {
            return new MenuEntry(this);
        }
    }
}
