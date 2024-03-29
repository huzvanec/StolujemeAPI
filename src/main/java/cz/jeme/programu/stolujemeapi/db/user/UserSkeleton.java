package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;

public interface UserSkeleton extends Skeleton {
    @NotNull String email();

    @NotNull String name();

    @NotNull String passwordHash();

    @NotNull String passwordSalt();
}