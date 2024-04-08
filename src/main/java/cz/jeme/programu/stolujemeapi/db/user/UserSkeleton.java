package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class UserSkeleton implements Skeleton {
    private final @NotNull String email;
    private final @NotNull String name;
    private final @NotNull String passwordHash;
    private final @NotNull String passwordSalt;

    private UserSkeleton(final @NotNull Builder builder) {
        email = Objects.requireNonNull(builder.email, "email");
        name = Objects.requireNonNull(builder.name, "name");
        passwordHash = Objects.requireNonNull(builder.passwordHash, "passwordHash");
        passwordSalt = Objects.requireNonNull(builder.passwordSalt, "passwordSalt");
    }

    public @NotNull String email() {
        return email;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String passwordHash() {
        return passwordHash;
    }

    public @NotNull String passwordSalt() {
        return passwordSalt;
    }

    public static final class Builder implements Skeleton.Builder<Builder, UserSkeleton> {
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable String passwordHash;
        private @Nullable String passwordSalt;

        public @NotNull Builder email(final @NotNull String email) {
            this.email = email;
            return this;
        }

        public @NotNull Builder name(final @NotNull String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder passwordHash(final @NotNull String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public @NotNull Builder passwordSalt(final @NotNull String passwordSalt) {
            this.passwordSalt = passwordSalt;
            return this;
        }

        @Override
        public @NotNull UserSkeleton build() {
            return new UserSkeleton(this);
        }
    }
}