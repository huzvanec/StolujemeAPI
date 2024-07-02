package cz.jeme.programu.stolujemeapi.db.photo;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record Photo(
        int id,
        int mealId,
        int userId,
        @NotNull UUID uuid,
        @NotNull File file,
        long fileSize,
        @NotNull LocalDateTime uploadedTime
) implements Entry {
    private Photo(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.id, "id"),
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.userId, "userId"),
                Objects.requireNonNull(builder.uuid, "mealUuid"),
                Objects.requireNonNull(builder.file, "file"),
                Objects.requireNonNull(builder.fileSize, "fileSize"),
                Objects.requireNonNull(builder.uploadedTime, "uploadedTime")
        );
        if (!file.exists())
            throw new IllegalArgumentException("This file does not exist!");
    }

    @ApiStatus.Internal
    public static final class Builder implements Entry.Builder<Builder, Photo> {
        private @Nullable Integer id;
        private @Nullable Integer mealId;
        private @Nullable Integer userId;
        private @Nullable UUID uuid;
        private @Nullable File file;
        private @Nullable Long fileSize;
        private @Nullable LocalDateTime uploadedTime;

        @Override
        public @NotNull Photo build() {
            return new Photo(this);
        }

        @Override
        public @NotNull Builder id(final int id) {
            this.id = id;
            return this;
        }

        public @NotNull Builder mealId(final int mealId) {
            this.mealId = mealId;
            return this;
        }

        public @NotNull Builder userId(final int userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull Builder uuid(final @Nullable UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder file(final @Nullable File file) {
            this.file = file;
            return this;
        }

        public @NotNull Builder fileSize(final long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public @NotNull Builder uploadedTime(final @Nullable LocalDateTime uploadedTime) {
            this.uploadedTime = uploadedTime;
            return this;
        }
    }
}
