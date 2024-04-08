package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

public final class User implements Entry {
    private final int id;
    private final @NotNull String email;
    private final @NotNull String name;
    private final boolean verified;
    private final @NotNull LocalDateTime registered;
    private final @NotNull String passwordHash;
    private final @NotNull String passwordSalt;

    private User(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        email = Objects.requireNonNull(builder.email, "email");
        name = Objects.requireNonNull(builder.name, "name");
        verified = Objects.requireNonNull(builder.verified, "verified");
        registered = Objects.requireNonNull(builder.registered, "registered");
        passwordHash = Objects.requireNonNull(builder.passwordHash, "passwordHash");
        passwordSalt = Objects.requireNonNull(builder.passwordSalt, "passwordSalt");
    }

    public @NotNull String email() {
        return email;
    }

    public @NotNull String name() {
        return name;
    }

    public boolean verified() {
        return verified;
    }

    public @NotNull LocalDateTime registered() {
        return registered;
    }

    public @NotNull String passwordHash() {
        return passwordHash;
    }

    public @NotNull String passwordSalt() {
        return passwordSalt;
    }

    @Override
    public int id() {
        return id;
    }

    static final class Builder implements Entry.Builder<Builder, User> {
        private @Nullable Integer id;
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable Boolean verified;
        private @Nullable LocalDateTime registered;
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

        public @NotNull Builder verified(final boolean verified) {
            this.verified = verified;
            return this;
        }

        public @NotNull Builder registered(final @NotNull LocalDateTime registered) {
            this.registered = registered;
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
        public @NotNull User build() {
            return new User(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }
    }
}