package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Session implements Entry {
    private final int id;
    private final int userId;
    private final @NotNull LocalDateTime creationTime;
    private final @NotNull LocalDateTime expirationTime;
    private final @NotNull String token;
    private final @NotNull Duration duration;

    private Session(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        userId = Objects.requireNonNull(builder.userId, "userId");
        creationTime = Objects.requireNonNull(builder.creationTime, "creationTime");
        expirationTime = Objects.requireNonNull(builder.expirationTime, "expirationTime");
        token = Objects.requireNonNull(builder.token, "token");
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

    public @NotNull String token() {
        return token;
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
        if (!(o instanceof final Session session)) return false;

        return id == session.id && userId == session.userId && creationTime.equals(session.creationTime) && expirationTime.equals(session.expirationTime) && token.equals(session.token);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + userId;
        result = 31 * result + creationTime.hashCode();
        result = 31 * result + expirationTime.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "Session{" +
               "id=" + id +
               ", userId=" + userId +
               ", creationTime=" + creationTime +
               ", expirationTime=" + expirationTime +
               ", token='" + token + '\'' +
               ", duration=" + duration +
               '}';
    }

    static final class Builder implements Entry.Builder<Builder, Session> {
        private @Nullable Integer id;
        private @Nullable Integer userId;
        private @Nullable LocalDateTime creationTime;
        private @Nullable LocalDateTime expirationTime;
        private @Nullable String token;

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

        public @NotNull Builder token(final @NotNull String token) {
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