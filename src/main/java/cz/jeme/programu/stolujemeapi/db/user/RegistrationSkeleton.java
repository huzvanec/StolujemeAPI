package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

public record RegistrationSkeleton(
        @NotNull String email,
        @NotNull String name,
        @NotNull Canteen canteen,
        @NotNull String passwordHash,
        @NotNull String passwordSalt,
        @NotNull String code,
        @NotNull Duration duration
) implements Skeleton {
    private RegistrationSkeleton(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.email, "email"),
                Objects.requireNonNull(builder.name, "name"),
                Objects.requireNonNull(builder.canteen, "canteen"),
                Objects.requireNonNull(builder.passwordHash, "passwordHash"),
                Objects.requireNonNull(builder.passwordSalt, "passwordSalt"),
                Objects.requireNonNull(builder.code, "code"),
                Objects.requireNonNull(builder.duration, "duration")
        );
    }

    public static final class Builder implements Skeleton.Builder<Builder, RegistrationSkeleton> {
        private @Nullable Duration duration;
        private @Nullable String email;
        private @Nullable String name;
        private @Nullable Canteen canteen;
        private @Nullable String passwordHash;
        private @Nullable String passwordSalt;
        private @Nullable String code;

        public @NotNull Builder duration(final @NotNull Duration duration) {
            this.duration = duration;
            return this;
        }

        public @NotNull Builder email(final @NotNull String email) {
            this.email = email;
            return this;
        }

        public @NotNull Builder name(final @NotNull String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder canteen(final @NotNull Canteen canteen) {
            this.canteen = canteen;
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

        public @NotNull Builder code(final @NotNull String code) {
            this.code = code;
            return this;
        }

        @Override
        public @NotNull RegistrationSkeleton build() {
            return new RegistrationSkeleton(this);
        }
    }
}