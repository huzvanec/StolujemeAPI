package cz.jeme.programu.stolujemeapi.db.verification;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;

public record StoluVerificationSkeleton(
        @Range(from = 1, to = 16_777_215) int userId,
        @NotNull Duration duration,
        @NotNull String code
) implements VerificationSkeleton {
    public StoluVerificationSkeleton(final @NotNull Verification verification) {
        this(verification.userId(), verification.duration(), verification.code());
    }
}
