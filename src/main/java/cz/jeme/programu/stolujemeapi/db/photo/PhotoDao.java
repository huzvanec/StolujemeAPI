package cz.jeme.programu.stolujemeapi.db.photo;

import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.sql.ResultWrapper;
import cz.jeme.programu.stolujemeapi.sql.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public enum PhotoDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;

    @Override
    public void init() {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String photosStatementStr = """
                    CREATE TABLE IF NOT EXISTS photos (
                    id_photo MEDIUMINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    id_meal MEDIUMINT UNSIGNED NOT NULL,
                    id_user MEDIUMINT UNSIGNED NOT NULL,
                    uuid UUID UNIQUE NOT NULL,
                    path VARCHAR(1000) UNIQUE NOT NULL,
                    file_size INT UNSIGNED NOT NULL,
                    upload_time DATETIME NOT NULL,
                    CONSTRAINT `fk_photo_meal`
                        FOREIGN KEY (id_meal) REFERENCES meals (id_meal),
                    CONSTRAINT `fk_photo_user`
                        FOREIGN KEY (id_user) REFERENCES users (id_user)
                    );
                    """;
            connection.prepareStatement(photosStatementStr).execute();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not initialize photo data access object!", e);
        }
    }

    public @NotNull Optional<Photo> photoByUuid(final @NotNull UUID uuid) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_photo, id_meal, id_user, path, upload_time
                    FROM photos WHERE uuid = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setUUID(uuid)
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Photo.Builder()
                    .id(result.getInt())
                    .mealId(result.getInt())
                    .userId(result.getInt())
                    .uuid(uuid)
                    .file(result.getFile())
                    .uploadedTime(result.getLocalDateTime())
                    .build()
            );
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find photo!", e);
        }
    }

    public @NotNull Photo insertPhoto(final @NotNull PhotoSkeleton skeleton) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    INSERT INTO photos (id_meal, id_user, uuid, path, file_size, upload_time)
                    VALUES (?, ?, ?, ?, ?, ?);
                    """;
            final LocalDateTime creationTime = LocalDateTime.now();
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setInt(skeleton.userId())
                    .setUUID(skeleton.uuid())
                    .setFile(skeleton.file())
                    .setInt((int) skeleton.file().length())
                    .setLocalDateTime(creationTime)
                    .executeGenerate();

            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Photo.Builder()
                    .id(result.getInt())
                    .mealId(skeleton.mealId())
                    .userId(skeleton.userId())
                    .uploadedTime(creationTime)
                    .uuid(skeleton.uuid())
                    .file(skeleton.file())
                    .build();
        } catch (final SQLException e) {
            throw new RuntimeException("Could not create photo!", e);
        }
    }

    public @NotNull List<Photo> photoByMealId(final int mealId) {
        try (final Connection connection = database.connection()) {
            // language=mariadb
            final String statementStr = """
                    SELECT id_photo, id_user, uuid, path, upload_time
                    FROM photos WHERE id_meal = ?;
                    """;
            final ResultWrapper result = StatementWrapper.wrapper(connection.prepareStatement(statementStr))
                    .setInt(mealId)
                    .executeQuery();

            final List<Photo> photos = new ArrayList<>();
            while (result.next()) {
                photos.add(new Photo.Builder()
                        .id(result.getInt())
                        .userId(result.getInt())
                        .mealId(mealId)
                        .uuid(result.getUUID())
                        .file(result.getFile())
                        .uploadedTime(result.getLocalDateTime())
                        .build()
                );
            }
            return photos;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find photo!", e);
        }
    }
}
