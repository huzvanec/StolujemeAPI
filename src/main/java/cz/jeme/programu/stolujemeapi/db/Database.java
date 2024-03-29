package cz.jeme.programu.stolujemeapi.db;

import cz.jeme.programu.stolujemeapi.db.session.SessionDao;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationDao;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {
    @NotNull Connection openConnection() throws SQLException;

    @NotNull UserDao getUserDao();

    @NotNull VerificationDao getVerificationDao();

    @NotNull SessionDao getSessionDao();
}