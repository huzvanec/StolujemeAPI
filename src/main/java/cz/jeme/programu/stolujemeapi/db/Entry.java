package cz.jeme.programu.stolujemeapi.db;

import org.jetbrains.annotations.NotNull;

public interface Entry {
    int id();

    interface Builder<B extends Builder<B, T>, T extends Entry> {
        @NotNull
        T build();

        @NotNull
        B id(final int id);
    }
}