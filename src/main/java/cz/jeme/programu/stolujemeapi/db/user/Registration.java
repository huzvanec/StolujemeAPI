package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record Registration(
        int id,
        @NotNull LocalDateTime creationTime,
        @NotNull LocalDateTime expirationTime,
        @NotNull String email,
        @NotNull String name,
        @NotNull Canteen canteen,
        @NotNull String passwordHash,
        @NotNull String passwordSalt,
        @NotNull String code
) implements Entry {
    public @NotNull Duration duration() {
        return Duration.between(creationTime, expirationTime);
    }

    public boolean expired() {
        return !expirationTime.isAfter(LocalDateTime.now()); // not using isBefore to exclude equals
    }

    private Registration(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.creationTime, "creationTime"),
                Objects.requireNonNull(builder.expirationTime, "expirationTime"),
                Objects.requireNonNull(builder.email, "email"),
                Objects.requireNonNull(builder.name, "name"),
                Objects.requireNonNull(builder.canteen, "canteen"),
                Objects.requireNonNull(builder.passwordHash, "passwordHash"),
                Objects.requireNonNull(builder.passwordSalt, "passwordSalt"),
                Objects.requireNonNull(builder.code, "code")
        );
    }

    @ApiStatus.Internal
    public static final class Builder implements Entry.Builder<Builder, Registration> {
        private @Nullable Integer id;
        private @Nullable LocalDateTime creationTime;
        private @Nullable LocalDateTime expirationTime;
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable Canteen canteen;
        private @Nullable String passwordHash;
        private @Nullable String passwordSalt;
        private @Nullable String code;

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder creationTime(final @Nullable LocalDateTime creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public @NotNull Builder expirationTime(final @Nullable LocalDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public @NotNull Builder email(final @Nullable String email) {
            this.email = email;
            return this;
        }

        public @NotNull Builder name(final @Nullable String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder canteen(final @Nullable Canteen canteen) {
            this.canteen = canteen;
            return this;
        }

        public @NotNull Builder passwordHash(final @Nullable String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public @NotNull Builder passwordSalt(final @Nullable String passwordSalt) {
            this.passwordSalt = passwordSalt;
            return this;
        }

        public @NotNull Builder code(final @Nullable String code) {
            this.code = code;
            return this;
        }

        @Override
        public @NotNull Registration build() {
            return new Registration(this);
        }
    }
}