package cz.jeme.programu.stolujemeapi.db;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Dao<E extends Entry, S extends Skeleton> {
    @ApiStatus.Internal
    void init();

    @NotNull
    Optional<E> byId(final int id);

    boolean existsId(final int id);

    @NotNull
    E insert(final @NotNull S skeleton);
}