package cz.jeme.programu.stolujemeapi.db.session;

import cz.jeme.programu.stolujemeapi.db.Dao;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SessionDao extends Dao<Session, SessionSkeleton> {
    @NotNull Optional<Session> getByToken(final @NotNull String token);

    boolean existsToken(final @NotNull String token);
}
