package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Dao;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface VerificationDao extends Dao<Verification, VerificationSkeleton> {
    static @NotNull VerificationDao dao() {
        return VerificationDaoImpl.INSTANCE;
    }

    @NotNull
    Optional<Verification> byCode(final @NotNull String code);

    boolean existsCode(final @NotNull String code);

    void verify(final @NotNull Verification verification);
}