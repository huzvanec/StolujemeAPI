package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

public record SessionSkeleton(
        int userId,
        @NotNull String token,
        @NotNull Duration duration
) implements Skeleton {
    private SessionSkeleton(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.userId, "userId"),
                Objects.requireNonNull(builder.token, "token"),
                Objects.requireNonNull(builder.duration, "duration")
        );
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