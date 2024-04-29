package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record Meal(
        int id,
        @NotNull UUID uuid,
        @NotNull Course course,
        @NotNull Canteen canteen,
        @Nullable String description
) implements Entry {
    private Meal(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.uuid, "mealUuid"),
                Objects.requireNonNull(builder.course, "course"),
                Objects.requireNonNull(builder.canteen, "canteen"),
                builder.description
        );
    }

    public enum Course {
        SOUP,
        MAIN,
        ADDITION
    }

    @ApiStatus.Internal
    public static final class Builder implements Entry.Builder<Builder, Meal> {
        private @Nullable Integer id;
        private @Nullable UUID uuid;
        private @Nullable Canteen canteen;
        private @Nullable Course course;
        private @Nullable String description;

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder uuid(final @Nullable UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder course(final @Nullable Course course) {
            this.course = course;
            return this;
        }

        public @NotNull Builder canteen(final @Nullable Canteen canteen) {
            this.canteen = canteen;
            return this;
        }

        public @NotNull Builder description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        @Override
        public @NotNull Meal build() {
            return new Meal(this);
        }
    }
}
