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
    private final @NotNull LocalDateTime creation;
    private final @NotNull LocalDateTime expiration;
    private final @NotNull String token;
    private final @NotNull Duration duration;

    private Session(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        userId = Objects.requireNonNull(builder.userId, "userId");
        creation = Objects.requireNonNull(builder.creation, "creation");
        expiration = Objects.requireNonNull(builder.expiration, "expiration");
        token = Objects.requireNonNull(builder.token, "token");
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

    public @NotNull String token() {
        return token;
    }

    public @NotNull Duration duration() {
        return duration;
    }

    public boolean expired() {
        return !expiration.isAfter(LocalDateTime.now()); // not using isBefore to exclude equals
    }

    @Override
    public boolean equals(final @NotNull Object o) {
        if (this == o) return true;
        if (!(o instanceof final Session session)) return false;

        return id == session.id && userId == session.userId && creation.equals(session.creation) && expiration.equals(session.expiration) && token.equals(session.token);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + userId;
        result = 31 * result + creation.hashCode();
        result = 31 * result + expiration.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }

    static final class Builder implements Entry.Builder<Builder, Session> {
        private @Nullable Integer id;
        private @Nullable Integer userId;
        private @Nullable LocalDateTime creation;
        private @Nullable LocalDateTime expiration;
        private @Nullable String token;

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