package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.time.LocalDateTime;

public interface Verification extends Entry {
    @Range(from = 1, to = 16_777_215) int userId();

    @NotNull LocalDateTime creation();

    @NotNull LocalDateTime expiration();

    @NotNull String code();

    @NotNull Duration duration();

    boolean expired();
}
