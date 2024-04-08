package cz.jeme.programu.stolujemeapi.db.user;

import cz.jeme.programu.stolujemeapi.db.Dao;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface UserDao extends Dao<User, UserSkeleton> {
    static @NotNull UserDao dao() {
        return UserDaoImpl.INSTANCE;
    }

    @NotNull
    Optional<User> byEmail(final @NotNull String email);

    @NotNull
    Optional<User> byName(final @NotNull String name);

    boolean existsEmail(final @NotNull String email);

    boolean existsName(final @NotNull String name);
}