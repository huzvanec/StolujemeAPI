package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

public final class VerificationSkeleton implements Skeleton {
    private final int userId;
    private final @NotNull Duration duration;
    private final @NotNull String code;

    private VerificationSkeleton(final @NotNull Builder builder) {
        userId = Objects.requireNonNull(builder.userId, "userId");
        duration = Objects.requireNonNull(builder.duration, "duration");
        code = Objects.requireNonNull(builder.code, "code");
    }

    public int userId() {
        return userId;
    }

    public @NotNull Duration duration() {
        return duration;
    }

    public @NotNull String code() {
        return code;
    }

    public static final class Builder implements Skeleton.Builder<Builder, VerificationSkeleton> {
        private @Nullable Integer userId;
        private @Nullable Duration duration;
        private @Nullable String code;

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder duration(final @NotNull Duration duration) {
            this.duration = duration;
            return this;
        }

        public @NotNull Builder code(final @NotNull String code) {
            this.code = code;
            return this;
        }

        @Override
        public @NotNull VerificationSkeleton build() {
            return new VerificationSkeleton(this);
        }
    }
}