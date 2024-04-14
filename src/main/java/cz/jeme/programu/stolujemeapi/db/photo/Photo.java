package cz.jeme.programu.stolujemeapi.db.photo;

import cz.jeme.programu.stolujemeapi.db.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Photo implements Entry {
    private final int id;
    private final int mealId;
    private final int userId;
    private final @NotNull UUID uuid;
    private final @NotNull File file;
    private final @NotNull LocalDateTime uploadedTime;

    private Photo(final @NotNull Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        mealId = Objects.requireNonNull(builder.mealId, "mealId");
        userId = Objects.requireNonNull(builder.userId, "userId");
        uuid = Objects.requireNonNull(builder.uuid, "mealUuid");
        file = Objects.requireNonNull(builder.file, "file");
        uploadedTime = Objects.requireNonNull(builder.uploadedTime, "uploadedTime");
    }

    @Override
    public int id() {
        return id;
    }

    public int mealId() {
        return mealId;
    }

    public int userId() {
        return userId;
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull File file() {
        return file;
    }

    public @NotNull LocalDateTime uploadedTime() {
        return uploadedTime;
    }

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final Photo photo)) return false;

        return id == photo.id && mealId == photo.mealId && userId == photo.userId && uuid.equals(photo.uuid) && file.equals(photo.file) && uploadedTime.equals(photo.uploadedTime);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + mealId;
        result = 31 * result + userId;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + file.hashCode();
        result = 31 * result + uploadedTime.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "Photo{" +
               "id=" + id +
               ", mealId=" + mealId +
               ", userId=" + userId +
               ", uuid=" + uuid +
               ", file=" + file.getAbsolutePath() +
               ", uploadedTime=" + uploadedTime +
               '}';
    }

    static final class Builder implements Entry.Builder<Builder, Photo> {
        private @Nullable Integer id;
        private @Nullable Integer mealId;
        private @Nullable Integer userId;
        private @Nullable UUID uuid;
        private @Nullable File file;
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

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder file(final @NotNull File file) {
            if (!file.exists()) throw new IllegalArgumentException("This file does not exist!");
            this.file = file;
            return this;
        }

        public @NotNull Builder uploadedTime(final @NotNull LocalDateTime uploadedTime) {
            this.uploadedTime = uploadedTime;
            return this;
        }
    }
}
