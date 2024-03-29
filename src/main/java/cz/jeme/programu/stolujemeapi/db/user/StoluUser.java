package cz.jeme.programu.stolujemeapi.db.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.LocalDateTime;

public record StoluUser(
        @Range(from = 1, to = 16_777_215) int id,
        @NotNull String email,
        @NotNull String name,
        boolean verified,
        @NotNull LocalDateTime registered,
        @NotNull String passwordHash,
        @NotNull String passwordSalt
) implements User {
}