package cz.jeme.programu.stolujemeapi.error;

import cz.jeme.programu.stolujemeapi.rest.control.UserController;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ApiErrorType {
    OK(null),
    UNKNOWN(null),
    @ApiStatus.Internal MISSING_PARAMETER(null),
    NAME_LENGTH_INVALID("name length must be between %d and %d characters".formatted(
            UserController.NAME_LENGTH_MIN,
            UserController.NAME_LENGTH_MAX
    )),
    NAME_CONTENTS_INVALID("name does not match '%s'".formatted(
            UserController.NAME_REGEX
    )),
    NAME_NOT_UNIQUE("this name was already taken by another user"),

    PASSWORD_LENGTH_INVALID("password length must be between %d and %d characters".formatted(
            UserController.PASSWORD_LENGTH_MIN,
            UserController.PASSWORD_LENGTH_MAX
    )),

    EMAIL_LENGTH_INVALID("email length must be less than %d characters".formatted(
            UserController.EMAIL_LENGTH_MAX
    )),

    EMAIL_CONTENTS_INVALID("RFC 822 email validation failed"),

    EMAIL_NOT_UNIQUE("an account with this email already exists"),
    INVALID_CREDENTIALS("the name, email or password you provided is incorrect"),
    USER_ALREADY_VERIFIED("this user is already verified"),
    VERIFICATION_CODE_INVALID("the provided code does not match any existing verification code"),
    VERIFICATION_EXPIRED("this verification has already expired or the user was already verified"),
    UUID_CONTENTS_INVALID("this mealUuid is invalid"),
    MEAL_UUID_INVALID("this meal does not exist"),
    PHOTO_CONTENTS_INVALID("an error occurred while trying to process the photo"),
    PHOTO_UUID_INVALID("this photo does not exist"),
    CESKOLIPSKA_BETA("stolujeme is currently in development and is only available to users from ceskolipska.cz"),
    MISSING_AUTHENTICATION("Missing authentication (bearer token)"),
    AUTHENTICATION_INVALID("Invalid authentication"),
    RATING_INVALID("ratings must be between 0 and 10"),
    DATE_CONTENTS_INVALID("dates must be in format yyyy-MM-dd"),
    DATE_ORDER_INVALID("dates are in an incorrect order");

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
