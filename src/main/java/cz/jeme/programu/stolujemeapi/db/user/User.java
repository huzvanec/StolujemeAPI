package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public interface User extends Entry {
    @ApiStatus.Internal
    static @NotNull Builder builder() {
        return new UserImpl.BuilderImpl();
    }

    @NotNull
    String email();

    @NotNull
    String name();

    boolean verified();

    @NotNull
    LocalDateTime registered();

    @NotNull
    String passwordHash();

    @NotNull
    String passwordSalt();

    interface Builder extends Entry.Builder<Builder, User> {
        @NotNull
        Builder email(final @NotNull String email);

        @NotNull
        Builder name(final @NotNull String name);

        @NotNull
        Builder verified(final boolean verified);

        @NotNull
        Builder registered(final @NotNull LocalDateTime registered);

        @NotNull
        Builder passwordHash(final @NotNull String passwordHash);

        @NotNull
        Builder passwordSalt(final @NotNull String passwordSalt);
    }
}