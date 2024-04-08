package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface SessionSkeleton extends Skeleton {
    static @NotNull Builder builder() {
        return new SessionSkeletonImpl.BuilderImpl();
    }

    int userId();

    @NotNull
    String token();

    @NotNull
    Duration duration();

    interface Builder extends Skeleton.Builder<Builder, SessionSkeleton> {
        @NotNull
        Builder userId(final int userId);

        @NotNull
        Builder token(final @NotNull String token);

        @NotNull
        Builder duration(final @NotNull Duration duration);
    }
}