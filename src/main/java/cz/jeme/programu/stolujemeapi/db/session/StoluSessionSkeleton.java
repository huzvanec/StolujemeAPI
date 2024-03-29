package cz.jeme.programu.stolujemeapi.db.session;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;

public record StoluSessionSkeleton(
        @Range(from = 1, to = 16_777_215) int userId,
        @NotNull String token,
        @NotNull Duration duration
) implements SessionSkeleton {
    public StoluSessionSkeleton(final @NotNull Session session) {
        this(session.userId(), session.token(), session.duration());
    }
}
