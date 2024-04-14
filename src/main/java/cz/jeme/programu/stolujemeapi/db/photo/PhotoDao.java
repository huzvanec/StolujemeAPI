package cz.jeme.programu.stolujemeapi.db.photo;

import cz.jeme.programu.stolujemeapi.db.Dao;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StatementWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public enum PhotoDao implements Dao {
    INSTANCE;

    private final @NotNull Database database = Database.INSTANCE;
    private final @NotNull StatementWrapper wrapper = StatementWrapper.wrapper();

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
                    path VARCHAR(300) UNIQUE NOT NULL,
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
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setString(uuid.toString())
                    .unwrap()
                    .executeQuery();
            if (!result.next()) return Optional.empty();
            return Optional.of(new Photo.Builder()
                    .id(result.getInt(1))
                    .mealId(result.getInt(2))
                    .userId(result.getInt(3))
                    .uuid(uuid)
                    .file(new File(result.getString(4)))
                    .uploadedTime(result.getTimestamp(5).toLocalDateTime())
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
            final LocalDateTime creation = LocalDateTime.now();
            final PreparedStatement statement = wrapper
                    .wrap(connection.prepareStatement(statementStr, Statement.RETURN_GENERATED_KEYS))
                    .setInt(skeleton.mealId())
                    .setInt(skeleton.userId())
                    .setString(skeleton.uuid().toString())
                    .setString(skeleton.file().getAbsolutePath())
                    .setInt((int) skeleton.file().length())
                    .setTimestamp(Timestamp.valueOf(creation))
                    .unwrap();
            statement.execute();
            final ResultSet result = statement.getGeneratedKeys();
            if (!result.next()) throw new RuntimeException("Id was not returned!");
            return new Photo.Builder()
                    .id(result.getInt(1))
                    .mealId(skeleton.mealId())
                    .userId(skeleton.userId())
                    .uploadedTime(creation)
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
            final ResultSet result = wrapper.wrap(connection.prepareStatement(statementStr))
                    .setInt(mealId)
                    .unwrap()
                    .executeQuery();
            final List<Photo> photos = new ArrayList<>();
            while (result.next()) {
                photos.add(new Photo.Builder()
                        .id(result.getInt(1))
                        .userId(result.getInt(2))
                        .mealId(mealId)
                        .uuid(UUID.fromString(result.getString(3)))
                        .file(new File(result.getString(4)))
                        .uploadedTime(result.getTimestamp(5).toLocalDateTime())
                        .build()
                );
            }
            return photos;
        } catch (final SQLException e) {
            throw new RuntimeException("Could not find photo!", e);
        }
    }
}
