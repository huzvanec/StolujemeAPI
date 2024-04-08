package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Verification implements Entry {
    private final int id;
    private final int userId;
    private final @NotNull LocalDateTime creation;
    private final @NotNull LocalDateTime expiration;
    private final @NotNull String code;
    private final @NotNull Duration duration;

    private Verification(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        userId = Objects.requireNonNull(builder.userId, "userId");
        creation = Objects.requireNonNull(builder.creation, "creation");
        expiration = Objects.requireNonNull(builder.expiration, "expiration");
        code = Objects.requireNonNull(builder.code, "code");
        duration = Duration.between(creation, expiration);
    }

    @Override
    public int id() {
        return id;
    }

    public int userId() {
        return userId;
    }

    public @NotNull LocalDateTime creation() {
        return creation;
    }

    public @NotNull LocalDateTime expiration() {
        return expiration;
    }

    public @NotNull String code() {
        return code;
    }

    public @NotNull Duration duration() {
        return duration;
    }

    public boolean expired() {
        return !expiration.isAfter(LocalDateTime.now()); // not using isBefore to exclude equals
    }

    static final class Builder implements Entry.Builder<Builder, Verification> {
        private @Nullable Integer id;
        private @Nullable Integer userId;
        private @Nullable LocalDateTime creation;
        private @Nullable LocalDateTime expiration;
        private @Nullable String code;

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder creation(final @NotNull LocalDateTime creation) {
            this.creation = creation;
            return this;
        }

        public @NotNull Builder expiration(final @NotNull LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }

        public @NotNull Builder code(final @NotNull String code) {
            this.code = code;
            return this;
        }

        @Override
        public @NotNull Verification build() {
            return new Verification(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }
    }
}