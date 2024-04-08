package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface VerificationSkeleton extends Skeleton {
    static @NotNull Builder builder() {
        return new VerificationSkeletonImpl.BuilderImpl();
    }

    int userId();

    @NotNull
    Duration duration();

    @NotNull
    String code();

    interface Builder extends Skeleton.Builder<Builder, VerificationSkeleton> {
        @NotNull
        Builder userId(final int userId);

        @NotNull
        Builder duration(final @NotNull Duration duration);

        @NotNull
        Builder code(final @NotNull String code);
    }
}
