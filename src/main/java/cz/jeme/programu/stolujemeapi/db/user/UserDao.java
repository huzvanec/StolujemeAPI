package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Dao;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface UserDao extends Dao<User, UserSkeleton> {
    @NotNull Optional<User> getByEmail(final @NotNull String email);

    @NotNull Optional<User> getByName(final @NotNull String name);

    boolean existsEmail(final @NotNull String email);

    boolean existsName(final @NotNull String name);
}