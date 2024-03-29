package cz.jeme.programu.stolujemeapi.db;

import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.db.session.SessionDao;
import cz.jeme.programu.stolujemeapi.db.session.StoluSessionDao;
import cz.jeme.programu.stolujemeapi.db.user.StoluUserDao;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.verification.StoluVerificationDao;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationDao;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

public final class StoluDatabase implements Database {
    private static @Nullable StoluDatabase instance;
    private final @NotNull UserDao userDao = new StoluUserDao(this);

    private final @NotNull VerificationDao verificationDao = new StoluVerificationDao(this);

    private final @NotNull SessionDao sessionDao = new StoluSessionDao(this);

    private final @NotNull String url;

    private final @NotNull String user;

    private final @NotNull String password;

    private StoluDatabase() {
        url = require(Stolujeme.getArgs().get("url"), "url");
        user = require(Stolujeme.getArgs().get("user"), "user");
        password = require(Stolujeme.getArgs().get("password"), "password");

        Stolujeme.getLogger().info("Testing database connection...");
        try (Connection connection = openConnection()) {
            Stolujeme.getLogger().info("Established connection to database");
            DatabaseMetaData meta = connection.getMetaData();
            String driver = "%s (%s)".formatted(meta.getDriverName(), meta.getDriverVersion());
            String jdbcVersion = "%d.%d".formatted(meta.getDriverMajorVersion(), meta.getDriverMinorVersion());
            Stolujeme.getLogger().info("Using driver: " + driver);
            Stolujeme.getLogger().info("JDBC version: " + jdbcVersion);
            // language=mariadb
            ResultSet result = connection.prepareStatement("SELECT VERSION();").executeQuery();
            if (!result.next()) throw new RuntimeException("Database version was not returned!");
            Stolujeme.getLogger().info("Distribution: " + result.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to database!");
        }
        Stolujeme.getLogger().info("Database connection test success");
    }

    private static <T> @NotNull T require(final @Nullable T arg,
                                          final @NotNull String name) {
        if (arg == null)
            throw new IllegalArgumentException("Missing required argument: %s!".formatted(name));
        return arg;
    }

    public static synchronized @NotNull StoluDatabase getInstance() {
        if (instance == null)
            instance = new StoluDatabase();
        return instance;
    }

    @PostConstruct
    private void postConstruct() {
        userDao.init();
        verificationDao.init();
        sessionDao.init();
    }

    @Override
    public @NotNull Connection openConnection() throws SQLException {
        final Connection connection;
        try {
            connection = DriverManager.getConnection(
                    url,
                    user,
                    password
            );
        } catch (SQLException e) {
            Stolujeme.getLogger().error("Could not establish connection to database!");
            throw e;
        }
        return connection;
    }

    @Override
    public @NotNull UserDao getUserDao() {
        return userDao;
    }

    @Override
    public @NotNull VerificationDao getVerificationDao() {
        return verificationDao;
    }

    @Override
    public @NotNull SessionDao getSessionDao() {
        return sessionDao;
    }
}
