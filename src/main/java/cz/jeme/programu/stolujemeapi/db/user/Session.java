package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record Session(
        int id,
        int userId,
        @NotNull LocalDateTime creationTime,
        @NotNull LocalDateTime expirationTime,
        @NotNull String token
) implements Entry {
    private Session(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.userId, "userId"),
                Objects.requireNonNull(builder.creationTime, "creationTime"),
                Objects.requireNonNull(builder.expirationTime, "expirationTime"),
                Objects.requireNonNull(builder.token, "token")
        );
    }

    public @NotNull Duration duration() {
        return Duration.between(creationTime, expirationTime);
    }

    public boolean expired() {
        return !expirationTime.isAfter(LocalDateTime.now()); // not using isBefore to exclude equals
    }

    @ApiStatus.Internal
    public static final class Builder implements Entry.Builder<Builder, Session> {
        private @Nullable Integer id;
        private @Nullable Integer userId;
        private @Nullable LocalDateTime creationTime;
        private @Nullable LocalDateTime expirationTime;
        private @Nullable String token;

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder creationTime(final @Nullable LocalDateTime creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public @NotNull Builder expirationTime(final @Nullable LocalDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public @NotNull Builder token(final @Nullable String token) {
            this.token = token;
            return this;
        }

        @Override
        public @NotNull Session build() {
            return new Session(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }
    }
}