package cz.jeme.programu.stolujemeapi.sql;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Wrapper;

public interface WrapperWrapper<W extends WrapperWrapper<W, T>, T extends Wrapper> {
    @NotNull
    T unwrap() throws SQLException;

    @NotNull
    W wrap(final @NotNull T wrapper) throws SQLException;

    @NotNull
    W clear() throws SQLException;

    boolean wrapped();
}