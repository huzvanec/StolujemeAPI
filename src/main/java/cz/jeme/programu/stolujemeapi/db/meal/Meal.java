package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class Meal implements Entry {
    private final int id;
    private final @NotNull UUID uuid;
    private final @NotNull Course course;

    private Meal(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        uuid = Objects.requireNonNull(builder.uuid, "uuid");
        course = Objects.requireNonNull(builder.course, "course");
    }

    @Override
    public int id() {
        return id;
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull Course course() {
        return course;
    }

    static final class Builder implements Entry.Builder<Builder, Meal> {
        private @Nullable Integer id;
        private @Nullable UUID uuid;
        private @Nullable Course course;

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder course(final @NotNull Course course) {
            this.course = course;
            return this;
        }

        @Override
        public @NotNull Meal build() {
            return new Meal(this);
        }
    }

    public enum Course {
        SOUP,
        MAIN,
        ADDITION
    }

    public enum Role {
        SOUP,
        ONE("1"),
        TWO("2"),
        THREE("3"),
        ADDITION;

        private final @NotNull String role;

        Role(final @NotNull String role) {
            this.role = role;
        }

        Role() {
            role = name();
        }


        @Override
        public @NotNull String toString() {
            return role;
        }

        public @NotNull Course toCourse() {
            return switch (this) {
                case SOUP -> Course.SOUP;
                case ONE, TWO, THREE -> Course.MAIN;
                case ADDITION -> Course.ADDITION;
            };
        }
    }
}
