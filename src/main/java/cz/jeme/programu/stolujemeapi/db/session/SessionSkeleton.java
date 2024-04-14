package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

public final class SessionSkeleton implements Skeleton {
    private final int userId;
    private final @NotNull String token;
    private final @NotNull Duration duration;

    private SessionSkeleton(final @NotNull Builder builder) {
        userId = Objects.requireNonNull(builder.userId, "userId");
        token = Objects.requireNonNull(builder.token, "token");
        duration = Objects.requireNonNull(builder.duration, "duration");
    }

    public int userId() {
        return userId;
    }

    public @NotNull String token() {
        return token;
    }

    public @NotNull Duration duration() {
        return duration;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof final SessionSkeleton that)) return false;

        return userId == that.userId && token.equals(that.token) && duration.equals(that.duration);
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + token.hashCode();
        result = 31 * result + duration.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "SessionSkeleton{" +
               "userId=" + userId +
               ", token='" + token + '\'' +
               ", duration=" + duration +
               '}';
    }

    public static final class Builder implements Skeleton.Builder<Builder, SessionSkeleton> {
        private @Nullable Integer userId;
        private @Nullable String token;
        private @Nullable Duration duration;

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder token(final @NotNull String token) {
            this.token = token;
            return this;
        }

        public @NotNull Builder duration(final @NotNull Duration duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public @NotNull SessionSkeleton build() {
            return new SessionSkeleton(this);
        }
    }
}