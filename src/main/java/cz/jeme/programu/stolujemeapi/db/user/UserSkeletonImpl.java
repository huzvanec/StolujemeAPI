package cz.jeme.programu.stolujemeapi.db.user;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
final class UserSkeletonImpl implements UserSkeleton {
    private final @NotNull String email;
    private final @NotNull String name;
    private final @NotNull String passwordHash;
    private final @NotNull String passwordSalt;

    private UserSkeletonImpl(final @NotNull BuilderImpl builder) {
        email = Objects.requireNonNull(builder.email, "email");
        name = Objects.requireNonNull(builder.name, "name");
        passwordHash = Objects.requireNonNull(builder.passwordHash, "passwordHash");
        passwordSalt = Objects.requireNonNull(builder.passwordSalt, "passwordSalt");
    }

    @Override
    public @NotNull String email() {
        return email;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull String passwordHash() {
        return passwordHash;
    }

    @Override
    public @NotNull String passwordSalt() {
        return passwordSalt;
    }

    static final class BuilderImpl implements UserSkeleton.Builder {
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable String passwordHash;
        private @Nullable String passwordSalt;

        @Override
        public @NotNull Builder email(final @NotNull String email) {
            this.email = email;
            return this;
        }

        @Override
        public @NotNull Builder name(final @NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public @NotNull Builder passwordHash(final @NotNull String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        @Override
        public @NotNull Builder passwordSalt(final @NotNull String passwordSalt) {
            this.passwordSalt = passwordSalt;
            return this;
        }

        @Override
        public @NotNull UserSkeleton build() {
            return new UserSkeletonImpl(this);
        }
    }
}