package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Canteen;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.user.UserSkeleton;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.apache.commons.validator.routines.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;

@RestController
public final class RegisterController {
    public static final int NAME_LENGTH_MIN = 3;
    public static final int NAME_LENGTH_MAX = 30;
    // language=regexp
    public static final @NotNull String NAME_REGEX = "^[a-zA-Z0-9 ._-]+$";
    public static final int PASSWORD_LENGTH_MIN = 5;
    public static final int PASSWORD_LENGTH_MAX = 100;
    public static final int EMAIL_LENGTH_MAX = 250;

    private RegisterController() {
    }

    @PostMapping("/register")
    @ResponseBody
    private @NotNull Response register(final @NotNull @RequestBody RegisterRequest request) {
        final String name = ApiUtils.validate(
                request.name(),
                "name",
                this::nameValid
        );
        final String email = ApiUtils.validate(
                request.email(),
                "email",
                this::emailValid
        );
        final String password = ApiUtils.validate(
                request.password(),
                "password",
                this::passwordValid
        );

        final String salt = CryptoUtils.genSalt();
        final String hash;
        try {
            hash = CryptoUtils.hash(password, salt);
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Could not hash password!", e);
        }

        // TODO
        final Canteen canteen = Canteen.CESKOLIPSKA;
        if (!email.endsWith("@ceskolipska.cz"))
            throw new InvalidParamException("email", ApiErrorType.CESKOLIPSKA_BETA);

        UserDao.INSTANCE.insertUser(new UserSkeleton.Builder()
                .email(email)
                .name(name)
                .canteen(canteen)
                .passwordHash(hash)
                .passwordSalt(salt)
                .build()
        );

        return new RegisterResponse(
                email, name
        );
    }

    public @NotNull ApiErrorType nameValid(final @NotNull String name) {
        final int length = name.length();
        if (length < RegisterController.NAME_LENGTH_MIN || length > RegisterController.NAME_LENGTH_MAX)
            return ApiErrorType.NAME_LENGTH_INVALID;
        if (!name.matches(RegisterController.NAME_REGEX)) return ApiErrorType.NAME_CONTENTS_INVALID;
        if (UserDao.INSTANCE.existsUserName(name)) return ApiErrorType.NAME_NOT_UNIQUE;
        return ApiErrorType.OK;
    }

    public @NotNull ApiErrorType passwordValid(final @NotNull String password) {
        final int length = password.length();
        if (length < RegisterController.PASSWORD_LENGTH_MIN || length > RegisterController.PASSWORD_LENGTH_MAX)
            return ApiErrorType.PASSWORD_LENGTH_INVALID;
        return ApiErrorType.OK;
    }

    public @NotNull ApiErrorType emailValid(final @NotNull String email) {
        if (email.length() > RegisterController.EMAIL_LENGTH_MAX) return ApiErrorType.EMAIL_LENGTH_INVALID;
        if (!EmailValidator.getInstance().isValid(email)) return ApiErrorType.EMAIL_CONTENTS_INVALID;
        if (UserDao.INSTANCE.existsUserEmail(email)) return ApiErrorType.EMAIL_NOT_UNIQUE;
        return ApiErrorType.OK;
    }

    public record RegisterRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("name")
            @Nullable String name,

            @JsonProperty("password")
            @Nullable String password
    ) implements Request {
    }

    public record RegisterResponse(
            @JsonProperty("email")
            @NotNull String email,

            @JsonProperty("name")
            @NotNull String name
    ) implements Response {
        @Override
        public @NotNull String sectionName() {
            return "registration";
        }
    }
}