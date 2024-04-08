package cz.jeme.programu.stolujemeapi.db.user;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

@ApiStatus.Internal
final class UserImpl implements User {
    private final int id;
    private final @NotNull String email;
    private final @NotNull String name;
    private final boolean verified;
    private final @NotNull LocalDateTime registered;
    private final @NotNull String passwordHash;
    private final @NotNull String passwordSalt;

    private UserImpl(final @NotNull BuilderImpl builder) {
        id = Objects.requireNonNull(builder.id, "id");
        email = Objects.requireNonNull(builder.email, "email");
        name = Objects.requireNonNull(builder.name, "name");
        verified = Objects.requireNonNull(builder.verified, "verified");
        registered = Objects.requireNonNull(builder.registered, "registered");
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
    public boolean verified() {
        return verified;
    }

    @Override
    public @NotNull LocalDateTime registered() {
        return registered;
    }

    @Override
    public @NotNull String passwordHash() {
        return passwordHash;
    }

    @Override
    public @NotNull String passwordSalt() {
        return passwordSalt;
    }

    @Override
    public int id() {
        return id;
    }

    static final class BuilderImpl implements User.Builder {
        private @Nullable Integer id;
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable Boolean verified;
        private @Nullable LocalDateTime registered;
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
        public @NotNull Builder verified(final boolean verified) {
            this.verified = verified;
            return this;
        }

        @Override
        public @NotNull Builder registered(final @NotNull LocalDateTime registered) {
            this.registered = registered;
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
        public @NotNull User build() {
            return new UserImpl(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }
    }
}