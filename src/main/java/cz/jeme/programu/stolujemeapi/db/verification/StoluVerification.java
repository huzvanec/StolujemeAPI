package cz.jeme.programu.stolujemeapi.db.verification;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.time.LocalDateTime;

public final class StoluVerification implements Verification {
    private final @Range(from = 1, to = 16_777_215) int id;
    private final @Range(from = 1, to = 16_777_215) int userId;
    private final @NotNull LocalDateTime creation;
    private final @NotNull LocalDateTime expiration;
    private final @NotNull String code;
    private final @NotNull Duration duration;

    public StoluVerification(final @Range(from = 1, to = 16_777_215) int id,
                             final @Range(from = 1, to = 16_777_215) int userId,
                             final @NotNull LocalDateTime creation,
                             final @NotNull LocalDateTime expiration,
                             final @NotNull String code) {
        this.id = id;
        this.userId = userId;
        this.creation = creation;
        this.expiration = expiration;
        this.code = code;
        duration = Duration.between(creation, expiration);
    }

    @Override
    public @Range(from = 1, to = 16_777_215) int id() {
        return id;
    }

    @Override
    public @Range(from = 1, to = 16_777_215) int userId() {
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
}