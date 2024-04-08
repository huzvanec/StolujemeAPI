package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;

public interface UserSkeleton extends Skeleton {
    static @NotNull Builder builder() {
        return new UserSkeletonImpl.BuilderImpl();
    }

    @NotNull
    String email();

    @NotNull
    String name();

    @NotNull
    String passwordHash();

    @NotNull
    String passwordSalt();

    interface Builder extends Skeleton.Builder<Builder, UserSkeleton> {
        @NotNull
        Builder email(final @NotNull String email);

        @NotNull
        Builder name(final @NotNull String name);

        @NotNull
        Builder passwordHash(final @NotNull String passwordHash);

        @NotNull
        Builder passwordSalt(final @NotNull String passwordSalt);
    }
}