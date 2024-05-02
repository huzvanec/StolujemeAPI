package cz.jeme.programu.stolujemeapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum EnvVar {
    EMAIL_USERNAME,
    EMAIL_PASSWORD,
    DATABASE_URL,
    DATABASE_USER,
    DATABASE_PASSWORD,
    PHOTO_DIR;

    private final @Nullable String value;
    private final boolean required;

    EnvVar(final boolean required) {
        this.required = required;
        value = System.getenv(name());
        if (required && value == null)
            throw new IllegalArgumentException("Missing required environment variable: " + name());
    }

    EnvVar() {
        this(true); // all env variables are required by default
    }

    public boolean required() {
        return required;
    }

    public @Nullable String get() {
        return value;
    }

    public @NotNull String require() {
        if (!required)
            throw new RuntimeException("This environment variable is not required!");
        return Objects.requireNonNull(value);
    }


    @Override
    public @NotNull String toString() {
        return "EnvVar{" +
               "name='" + name() + '\'' +
               ", value='" + value + '\'' +
               '}';
    }
}