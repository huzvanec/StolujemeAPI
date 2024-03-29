package cz.jeme.programu.stolujemeapi.db.verification;

import cz.jeme.programu.stolujemeapi.db.Dao;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface VerificationDao extends Dao<Verification, VerificationSkeleton> {
    @NotNull Optional<Verification> getByCode(final @NotNull String code);

    boolean existsCode(final @NotNull String code);

    void verify(final @NotNull Verification verification);
}