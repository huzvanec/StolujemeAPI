package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public interface User extends Entry {
    @NotNull String email();

    @NotNull String name();

    boolean verified();

    @NotNull LocalDateTime registered();

    @NotNull String passwordHash();

    @NotNull String passwordSalt();
}