package cz.jeme.programu.stolujemeapi.db;

import cz.jeme.programu.stolujemeapi.Stolujeme;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Objects;

public enum Database {
    INSTANCE;

    private final @NotNull String url;
    private final @NotNull String user;
    private final @NotNull String password;

    Database() {
        url = Objects.requireNonNull(Stolujeme.args().get("url"), "url");
        user = Objects.requireNonNull(Stolujeme.args().get("user"), "user");
        password = Objects.requireNonNull(Stolujeme.args().get("password"), "password");

        Stolujeme.logger().info("Testing database connection...");
        try (final Connection connection = connection()) {
            Stolujeme.logger().info("Established connection to database");
            final DatabaseMetaData meta = connection.getMetaData();
            final String driver = "%s (%s)".formatted(meta.getDriverName(), meta.getDriverVersion());
            final String jdbcVersion = "%d.%d".formatted(meta.getDriverMajorVersion(), meta.getDriverMinorVersion());
            Stolujeme.logger().info("Using driver: {}", driver);
            Stolujeme.logger().info("JDBC version: {}", jdbcVersion);
            // language=mariadb
            final ResultSet result = connection.prepareStatement("SELECT VERSION();").executeQuery();
            if (!result.next()) throw new RuntimeException("Database version was not returned!");
            Stolujeme.logger().info("Distribution: {}", result.getString(1));
        } catch (final SQLException e) {
            throw new RuntimeException("Could not connect to database!");
        }
        Stolujeme.logger().info("Database connection test success");
    }

    public @NotNull Connection connection() throws SQLException {
        final Connection connection;
        try {
            connection = DriverManager.getConnection(
                    url,
                    user,
                    password
            );
        } catch (final SQLException e) {
            Stolujeme.logger().error("Could not establish connection to database!");
            throw e;
        }
        return connection;
    }
}