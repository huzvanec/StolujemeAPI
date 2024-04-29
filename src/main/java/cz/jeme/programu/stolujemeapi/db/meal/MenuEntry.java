package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record MenuEntry(
        int id,
        @NotNull Meal meal,
        @NotNull String mealName,
        @NotNull UUID uuid,
        @NotNull LocalDate date,
        @Nullable Integer courseNumber
) implements Entry {
    private MenuEntry(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.meal, "meal"),
                Objects.requireNonNull(builder.mealName, "mealName"),
                Objects.requireNonNull(builder.uuid, "uuid"),
                Objects.requireNonNull(builder.date, "date"),
                builder.courseNumber
        );
    }

    @ApiStatus.Internal
    public static final class Builder implements Entry.Builder<Builder, MenuEntry> {
        private @Nullable Integer id;
        private @Nullable Meal meal;
        private @Nullable String mealName;
        private @Nullable UUID uuid;
        private @Nullable LocalDate date;
        private @Nullable Integer courseNumber;


        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder meal(final @Nullable Meal meal) {
            this.meal = meal;
            return this;
        }

        public @NotNull Builder date(final @Nullable LocalDate date) {
            this.date = date;
            return this;
        }

        public @NotNull Builder courseNumber(final @Nullable Integer courseNumber) {
            this.courseNumber = courseNumber;
            return this;
        }

        public @NotNull Builder mealName(final @Nullable String mealName) {
            this.mealName = mealName;
            return this;
        }


        public @NotNull Builder uuid(final @Nullable UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        @Override
        public @NotNull MenuEntry build() {
            return new MenuEntry(this);
        }
    }
}
