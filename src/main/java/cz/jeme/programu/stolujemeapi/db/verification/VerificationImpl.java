package cz.jeme.programu.stolujemeapi.db.verification;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@ApiStatus.Internal
final class VerificationImpl implements Verification {
    private final int id;
    private final int userId;
    private final @NotNull LocalDateTime creation;
    private final @NotNull LocalDateTime expiration;
    private final @NotNull String code;
    private final @NotNull Duration duration;

    private VerificationImpl(final @NotNull BuilderImpl builder) {
        id = Objects.requireNonNull(builder.id, "id");
        userId = Objects.requireNonNull(builder.userId, "userId");
        creation = Objects.requireNonNull(builder.creation, "creation");
        expiration = Objects.requireNonNull(builder.expiration, "expiration");
        code = Objects.requireNonNull(builder.code, "code");
        duration = Duration.between(creation, expiration);
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
    public @NotNull LocalDateTime creation() {
        return creation;
    }

    @Override
    public @NotNull LocalDateTime expiration() {
        return expiration;
    }

    @Override
    public @NotNull String code() {
        return code;
    }

    @Override
    public @NotNull Duration duration() {
        return duration;
    }

    @Override
    public boolean expired() {
        return !expiration.isAfter(LocalDateTime.now()); // not using isBefore to exclude equals
    }

    static final class BuilderImpl implements Builder {
        private @Nullable Integer id;
        private @Nullable Integer userId;
        private @Nullable LocalDateTime creation;
        private @Nullable LocalDateTime expiration;
        private @Nullable String code;

        @Override
        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public @NotNull Builder creation(final @NotNull LocalDateTime creation) {
            this.creation = creation;
            return this;
        }

        @Override
        public @NotNull Builder expiration(final @NotNull LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }

        @Override
        public @NotNull Builder code(final @NotNull String code) {
            this.code = code;
            return this;
        }

        @Override
        public @NotNull Verification build() {
            return new VerificationImpl(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }
    }
}