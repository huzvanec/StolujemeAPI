package cz.jeme.programu.stolujemeapi.db;

import org.jetbrains.annotations.NotNull;

public interface Skeleton {
    interface Builder<B extends Builder<B, T>, T extends Skeleton> {
        @NotNull
        T build();
    }
}