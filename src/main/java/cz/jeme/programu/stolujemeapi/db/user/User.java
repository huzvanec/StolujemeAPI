package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.Canteen;
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
    private final @NotNull LocalDateTime registeredTime;
    private final @NotNull String passwordHash;
    private final @NotNull String passwordSalt;
    private final @NotNull Canteen canteen;

    private User(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        email = Objects.requireNonNull(builder.email, "email");
        name = Objects.requireNonNull(builder.name, "name");
        verified = Objects.requireNonNull(builder.verified, "verified");
        registeredTime = Objects.requireNonNull(builder.registeredTime, "registeredTime");
        passwordHash = Objects.requireNonNull(builder.passwordHash, "passwordHash");
        passwordSalt = Objects.requireNonNull(builder.passwordSalt, "passwordSalt");
        canteen = Objects.requireNonNull(builder.canteen, "canteen");
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

    public @NotNull LocalDateTime registeredTime() {
        return registeredTime;
    }

    public @NotNull String passwordHash() {
        return passwordHash;
    }

    public @NotNull String passwordSalt() {
        return passwordSalt;
    }

    public @NotNull Canteen canteen() {
        return canteen;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final User user)) return false;

        return id == user.id && verified == user.verified && email.equals(user.email) && name.equals(user.name) && registeredTime.equals(user.registeredTime) && passwordHash.equals(user.passwordHash) && passwordSalt.equals(user.passwordSalt) && canteen == user.canteen;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + email.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + Boolean.hashCode(verified);
        result = 31 * result + registeredTime.hashCode();
        result = 31 * result + passwordHash.hashCode();
        result = 31 * result + passwordSalt.hashCode();
        result = 31 * result + canteen.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "User{" +
               "id=" + id +
               ", email='" + email + '\'' +
               ", name='" + name + '\'' +
               ", verified=" + verified +
               ", registeredTime=" + registeredTime +
               ", passwordHash='" + passwordHash + '\'' +
               ", passwordSalt='" + passwordSalt + '\'' +
               ", canteen=" + canteen +
               '}';
    }

    static final class Builder implements Entry.Builder<Builder, User> {
        private @Nullable Integer id;
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable Boolean verified;
        private @Nullable LocalDateTime registeredTime;
        private @Nullable String passwordHash;
        private @Nullable String passwordSalt;
        private @Nullable Canteen canteen;

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

        public @NotNull Builder registeredTime(final @NotNull LocalDateTime registeredTime) {
            this.registeredTime = registeredTime;
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

        public @NotNull Builder canteen(final @NotNull Canteen canteen) {
            this.canteen = canteen;
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