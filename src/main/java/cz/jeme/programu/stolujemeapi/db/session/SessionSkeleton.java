package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;

public interface SessionSkeleton extends Skeleton {
    @Range(from = 1, to = 16_777_215) int userId();

    @NotNull String token();

    @NotNull Duration duration();
}