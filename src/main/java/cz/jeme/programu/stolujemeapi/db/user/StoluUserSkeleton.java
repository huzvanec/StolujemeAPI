package cz.jeme.programu.stolujemeapi.db.user;

import org.jetbrains.annotations.NotNull;

public record StoluUserSkeleton(
        @NotNull String email,
        @NotNull String name,
        @NotNull String passwordHash,
        @NotNull String passwordSalt
) implements UserSkeleton {
    public StoluUserSkeleton(final @NotNull User user) {
        this(user.email(), user.name(), user.passwordHash(), user.passwordSalt());
    }
}