package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record MealName(
        int id,
        int mealId,
        @NotNull String name
) implements Entry {
    private MealName(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.name, "name")
        );
    }

    @ApiStatus.Internal
    public static final class Builder implements Entry.Builder<Builder, MealName> {
        private @Nullable Integer id;
        private @Nullable Integer mealId;
        private @Nullable String name;

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
            return this;
        }

        public @NotNull Builder name(final @Nullable String name) {
            this.name = name;
            return this;
        }

        @Override
        public @NotNull MealName build() {
            return new MealName(this);
        }
    }
}
