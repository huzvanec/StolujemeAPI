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
    private final @NotNull LocalDateTime creationTime;
    private final @NotNull LocalDateTime expirationTime;
    private final @NotNull String code;
    private final @NotNull Duration duration;

    private Verification(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        userId = Objects.requireNonNull(builder.userId, "userId");
        creationTime = Objects.requireNonNull(builder.creationTime, "creationTime");
        expirationTime = Objects.requireNonNull(builder.expirationTime, "expirationTime");
        code = Objects.requireNonNull(builder.code, "code");
        duration = Duration.between(creationTime, expirationTime);
    }

    @Override
    public int id() {
        return id;
    }

    public int userId() {
        return userId;
    }

    public @NotNull LocalDateTime creationTime() {
        return creationTime;
    }

    public @NotNull LocalDateTime expirationTime() {
        return expirationTime;
    }

    public @NotNull String code() {
        return code;
    }

    public @NotNull Duration duration() {
        return duration;
    }

    public boolean expired() {
        return !expirationTime.isAfter(LocalDateTime.now()); // not using isBefore to exclude equals
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof final Verification that)) return false;

        return id == that.id && userId == that.userId && creationTime.equals(that.creationTime) && expirationTime.equals(that.expirationTime) && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + userId;
        result = 31 * result + creationTime.hashCode();
        result = 31 * result + expirationTime.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }

    static final class Builder implements Entry.Builder<Builder, Verification> {
        private @Nullable Integer id;
        private @Nullable Integer userId;
        private @Nullable LocalDateTime creationTime;
        private @Nullable LocalDateTime expirationTime;
        private @Nullable String code;

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder creationTime(final @NotNull LocalDateTime creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public @NotNull Builder expirationTime(final @NotNull LocalDateTime expirationTime) {
            this.expirationTime = expirationTime;
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