package cz.jeme.programu.stolujemeapi.canteen;

import com.fasterxml.jackson.databind.JsonNode;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class Canteen {
    // registries
    private static @NotNull Map<Class<? extends Canteen>, Canteen> classed = new HashMap<>();
    private static @NotNull Map<String, Canteen> numbered = new HashMap<>();
    private static @NotNull Map<String, Canteen> named = new HashMap<>();
    private static @NotNull Set<Canteen> canteens = new HashSet<>();

    // safe getter when called after init, canteens are unmodifiable
    public static @NotNull Set<Canteen> canteens() {
        return Canteen.canteens;
    }

    protected final @NotNull String number;
    protected final @NotNull String name;

    protected Canteen(final @NotNull String name, final @NotNull String number) {
        this.name = name;
        this.number = number;

        // register
        if (Canteen.numbered.containsKey(number))
            throw new IllegalArgumentException("Canteen number must be unique! Number: " + number);
        Canteen.numbered.put(number, this);
        Canteen.classed.put(getClass(), this);
        Canteen.named.put(name, this);
        Canteen.canteens.add(this);
    }

    @ApiStatus.Internal
    public static void init() {
        try (final ScanResult result = new ClassGraph()
                .acceptPackages(Canteen.class.getPackageName())
                .scan()) {
            for (final Class<?> clazz : result.getSubclasses(Canteen.class).loadClasses()) {
                final Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("Could not instantiate canteen!", e);
        } catch (final InstantiationException e) {
            throw new RuntimeException("Canteen can not be instantiated!", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not access canteen constructor!", e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Could not find canteen constructor!", e);
        }

        Canteen.classed = Collections.unmodifiableMap(Canteen.classed);
        Canteen.numbered = Collections.unmodifiableMap(Canteen.numbered);
        Canteen.named = Collections.unmodifiableMap(Canteen.named);
        Canteen.canteens = Collections.unmodifiableSet(Canteen.canteens);
    }

    public final @NotNull String name() {
        return name;
    }

    public final @NotNull String number() {
        return number;
    }

    public abstract boolean emailValid(final @NotNull String email);

    public abstract boolean mealValid(final @NotNull JsonNode meal);

    public abstract @NotNull Meal.Course translateCourse(final @NotNull String mealType);

    public static <T extends Canteen> @NotNull T fromClass(final @NotNull Class<T> clazz) {
        if (!Canteen.classed.containsKey(clazz))
            throw new IllegalArgumentException("No canteen found for class: " + clazz.getName());
        @SuppressWarnings("unchecked") final T typedCanteen = (T) Canteen.classed.get(clazz);
        return typedCanteen;
    }

    public static @NotNull Canteen fromNumber(final @NotNull String number) {
        final Canteen canteen = Canteen.numbered.get(number);
        if (canteen == null)
            throw new IllegalArgumentException("No canteen found for number: " + number);
        return canteen;
    }

    public static @NotNull Canteen fromEmail(final @NotNull String email) {
        final List<Canteen> canteens = Canteen.canteens.stream()
                .filter(canteen -> canteen.emailValid(email))
                .toList();

        if (canteens.isEmpty())
            throw new IllegalArgumentException("No canteen found for email: " + email);

        if (canteens.size() > 1)
            throw new RuntimeException("More than one canteen identified this as a valid email! Email: " + email);

        return canteens.get(0);
    }

    public static @NotNull Canteen fromName(final @NotNull String name) {
        final Canteen canteen = Canteen.named.get(name);
        if (canteen == null)
            throw new IllegalArgumentException("No canteen found for name: " + name);
        return canteen;
    }


    @Override
    public boolean equals(final @Nullable Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        final Canteen canteen = (Canteen) object;
        return number.equals(canteen.number) && name.equals(canteen.name);
    }

    @Override
    public int hashCode() {
        int result = number.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "Canteen{" +
               "number='" + number + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}