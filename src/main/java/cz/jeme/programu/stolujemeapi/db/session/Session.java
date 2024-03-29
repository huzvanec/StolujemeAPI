package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.time.LocalDateTime;

public interface Session extends Entry {
    @Range(from = 1, to = 16_777_215) int userId();

    @NotNull LocalDateTime creation();

    @NotNull LocalDateTime expiration();

    @NotNull String token();

    @NotNull Duration duration();

    boolean expired();
}