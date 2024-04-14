package cz.jeme.programu.stolujemeapi.db.meal;

import cz.jeme.programu.stolujemeapi.Canteen;
import cz.jeme.programu.stolujemeapi.db.Skeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class MealSkeleton implements Skeleton {
    private final @NotNull UUID uuid;
    private final @NotNull Meal.Course course;
    private final @NotNull Canteen canteen;

    private MealSkeleton(final @NotNull Builder builder) {
        this.uuid = builder.uuid;
        this.course = Objects.requireNonNull(builder.course, "course");
        this.canteen = Objects.requireNonNull(builder.canteen, "canteen");
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @NotNull Meal.Course course() {
        return course;
    }

    public @NotNull Canteen canteen() {
        return canteen;
    }

    @Override
    public final boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof final MealSkeleton that)) return false;

        return uuid.equals(that.uuid) && course == that.course && canteen == that.canteen;
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + course.hashCode();
        result = 31 * result + canteen.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "MealSkeleton{" +
               "uuid=" + uuid +
               ", course=" + course +
               ", canteen=" + canteen +
               '}';
    }

    public static final class Builder implements Skeleton.Builder<Builder, MealSkeleton> {
        private @NotNull UUID uuid = UUID.randomUUID();
        private @Nullable Meal.Course course;
        private @Nullable Canteen canteen;

        public @NotNull Builder uuid(final @NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public @NotNull Builder course(final @NotNull Meal.Course course) {
            this.course = course;
            return this;
        }

        public @NotNull Builder canteen(final @NotNull Canteen canteen) {
            this.canteen = canteen;
            return this;
        }


        @Override
        public @NotNull MealSkeleton build() {
            return new MealSkeleton(this);
        }
    }
}
