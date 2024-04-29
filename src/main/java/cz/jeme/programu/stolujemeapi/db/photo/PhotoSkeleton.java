package cz.jeme.programu.stolujemeapi.db.photo;

import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public record PhotoSkeleton(
        int mealId,
        int userId,
        @NotNull UUID uuid,
        @NotNull File file
) implements Skeleton {
    private PhotoSkeleton(final @NotNull Builder builder) {
        this(
                Objects.requireNonNull(builder.mealId, "mealId"),
                Objects.requireNonNull(builder.userId, "userId"),
                builder.uuid,
                Objects.requireNonNull(builder.file, "file")
        );
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
