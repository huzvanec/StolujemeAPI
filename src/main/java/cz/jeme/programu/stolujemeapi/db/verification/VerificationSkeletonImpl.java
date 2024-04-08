package cz.jeme.programu.stolujemeapi.db.verification;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

@ApiStatus.Internal
final class VerificationSkeletonImpl implements VerificationSkeleton {
    private final int userId;
    private final @NotNull Duration duration;
    private final @NotNull String code;

    private VerificationSkeletonImpl(final @NotNull BuilderImpl builder) {
        userId = Objects.requireNonNull(builder.userId, "userId");
        duration = Objects.requireNonNull(builder.duration, "duration");
        code = Objects.requireNonNull(builder.code, "code");
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
    public @NotNull Duration duration() {
        return duration;
    }

    @Override
    public @NotNull String code() {
        return code;
    }

    static final class BuilderImpl implements VerificationSkeleton.Builder {
        private @Nullable Integer userId;
        private @Nullable Duration duration;
        private @Nullable String code;

        @Override
        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public @NotNull Builder duration(final @NotNull Duration duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public @NotNull Builder code(final @NotNull String code) {
            this.code = code;
            return this;
        }

        @Override
        public @NotNull VerificationSkeleton build() {
            return new VerificationSkeletonImpl(this);
        }
    }
}