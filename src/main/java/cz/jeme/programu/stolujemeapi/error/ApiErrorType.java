package cz.jeme.programu.stolujemeapi.error;

import cz.jeme.programu.stolujemeapi.rest.control.RegisterController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ApiErrorType {
    OK(null),
    UNKNOWN(null),
    MISSING_PARAMETER(null),
    NAME_LENGTH_INVALID("name length must be between %d and %d characters".formatted(
            RegisterController.NAME_LENGTH_MIN,
            RegisterController.NAME_LENGTH_MAX
    )),
    NAME_CONTENTS_INVALID("name does not match '%s'".formatted(
            RegisterController.NAME_REGEX
    )),
    NAME_NOT_UNIQUE("this name was already taken by another user"),

    PASSWORD_LENGTH_INVALID("password length must be between %d and %d characters".formatted(
            RegisterController.PASSWORD_LENGTH_MIN,
            RegisterController.PASSWORD_LENGTH_MAX
    )),

    EMAIL_LENGTH_INVALID("email length must be less than %d characters".formatted(
            RegisterController.EMAIL_LENGTH_MAX
    )),

    EMAIL_CONTENTS_INVALID("RFC 822 email validation failed"),

    EMAIL_NOT_UNIQUE("an account with this email already exists"),
    INVALID_CREDENTIALS("the name, email or password you provided is incorrect"),
    USER_ALREADY_VERIFIED("This user is already verified"),
    VERIFICATION_CODE_INVALID("the provided code does not match any existing verification code"),
    VERIFICATION_EXPIRED("this verification has already expired or the user was already verified");

    private final @Nullable String message;

    ApiErrorType(final @Nullable String message) {
        this.message = message;
    }

    public @NotNull String getMessage() {
        if (message == null)
            throw new IllegalStateException("This error type does not have a message!");
        return message;
    }

    public boolean hasMessage() {
        return message != null;
    }
}
