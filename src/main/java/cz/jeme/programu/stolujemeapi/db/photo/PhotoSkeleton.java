package cz.jeme.programu.stolujemeapi.db.photo;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public final class PhotoSkeleton implements Skeleton {
    private final int mealId;
    private final int userId;
    private final @NotNull UUID uuid;
    private final @NotNull File file;

    private PhotoSkeleton(final @NotNull Builder builder) {
        mealId = Objects.requireNonNull(builder.mealId, "mealId");
        userId = Objects.requireNonNull(builder.userId, "userId");
        uuid = builder.uuid;
        file = Objects.requireNonNull(builder.file, "file");
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

    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final PhotoSkeleton that)) return false;

        return mealId == that.mealId && userId == that.userId && uuid.equals(that.uuid) && file.equals(that.file);
    }

    @Override
    public int hashCode() {
        int result = mealId;
        result = 31 * result + userId;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + file.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "PhotoSkeleton{" +
               "mealId=" + mealId +
               ", userId=" + userId +
               ", uuid=" + uuid +
               ", file=" + file.getAbsolutePath() +
               '}';
    }

    public static final class Builder implements Skeleton.Builder<Builder, PhotoSkeleton> {
        private @Nullable Integer mealId;
        private @Nullable Integer userId;
        private @NotNull UUID uuid = UUID.randomUUID();
        private @Nullable File file;

        @Override
        public @NotNull PhotoSkeleton build() {
            return new PhotoSkeleton(this);
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
    }
}
