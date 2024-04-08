package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public interface Verification extends Entry {
    @ApiStatus.Internal
    static @NotNull Builder builder() {
        return new VerificationImpl.BuilderImpl();
    }

    int userId();

    @NotNull
    LocalDateTime creation();

    @NotNull
    LocalDateTime expiration();

    @NotNull
    String code();

    @NotNull
    Duration duration();

    boolean expired();

    interface Builder extends Entry.Builder<Builder, Verification> {
        @NotNull
        Builder userId(final int userId);

        @NotNull
        Builder creation(final @NotNull LocalDateTime creation);

        @NotNull
        Builder expiration(final @NotNull LocalDateTime expiration);

        @NotNull
        Builder code(final @NotNull String code);
    }
}