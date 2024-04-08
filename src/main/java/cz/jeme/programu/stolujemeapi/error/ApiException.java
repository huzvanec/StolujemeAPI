package cz.jeme.programu.stolujemeapi.error;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApiException extends ResponseStatusException {
    private final @NotNull ApiErrorType type;
    private final @NotNull String message;

    public ApiException(final @NotNull HttpStatus status,
                        final @NotNull ApiErrorType type) {
        this(status, type, type.getMessage());
    }

    public ApiException(final @NotNull HttpStatus status) {
        this(status, ApiErrorType.UNKNOWN);
    }

    public ApiException(final @NotNull HttpStatus status,
                        final @NotNull String message) {
        this(status, ApiErrorType.UNKNOWN, message);
    }

    public ApiException(final @NotNull HttpStatus status,
                        final @NotNull ApiErrorType type,
                        final @NotNull String message) {
        super(status);
        this.type = type;
        this.message = message;
    }

    public @NotNull ApiErrorType getErrorType() {
        return type;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }
}