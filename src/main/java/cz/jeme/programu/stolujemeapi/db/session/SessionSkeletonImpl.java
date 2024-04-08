package cz.jeme.programu.stolujemeapi.db.session;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

@ApiStatus.Internal
final class SessionSkeletonImpl implements SessionSkeleton {
    private final int userId;
    private final @NotNull String token;
    private final @NotNull Duration duration;

    private SessionSkeletonImpl(final @NotNull BuilderImpl builder) {
        userId = Objects.requireNonNull(builder.userId, "userId");
        token = Objects.requireNonNull(builder.token, "token");
        duration = Objects.requireNonNull(builder.duration, "duration");
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
    public @NotNull String token() {
        return token;
    }

    @Override
    public @NotNull Duration duration() {
        return duration;
    }

    static final class BuilderImpl implements SessionSkeleton.Builder {
        private @Nullable Integer userId;
        private @Nullable String token;
        private @Nullable Duration duration;

        @Override
        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public @NotNull Builder token(final @NotNull String token) {
            this.token = token;
            return this;
        }

        @Override
        public @NotNull Builder duration(final @NotNull Duration duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public @NotNull SessionSkeleton build() {
            return new SessionSkeletonImpl(this);
        }
    }
}