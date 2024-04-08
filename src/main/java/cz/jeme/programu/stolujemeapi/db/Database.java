package cz.jeme.programu.stolujemeapi.db;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {
    static @NotNull Database db() {
        return DatabaseImpl.INSTANCE;
    }

    @NotNull
    Connection connection() throws SQLException;
}