package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.session.Session;
import cz.jeme.programu.stolujemeapi.db.session.SessionDao;
import cz.jeme.programu.stolujemeapi.db.session.SessionSkeleton;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@RestController
public final class LoginController {
    public static final @NotNull String CREDENTIALS = "#credentials";
    public static final @NotNull Duration SESSION_DURATION = Duration.ofDays(30);

    public static void throwIncorrectCredentials() {
        throw new InvalidParamException(LoginController.CREDENTIALS, ApiErrorType.INVALID_CREDENTIALS);
    }

    public static @NotNull Supplier<@NotNull InvalidParamException> supplyIncorrectCredentials() {
        return () -> new InvalidParamException(LoginController.CREDENTIALS, ApiErrorType.INVALID_CREDENTIALS);
    }

    private LoginController() {
    }

    @PostMapping("/login")
    @ResponseBody
    private @NotNull Response login(final @NotNull @RequestBody LoginRequest request) {
        final String email = ApiUtils.require(
                request.email(),
                "email"
        );

        final String password = ApiUtils.require(
                request.password(),
                "password"
        );

        final User user = UserDao.INSTANCE.userByEmail(email)
                .orElseThrow(LoginController.supplyIncorrectCredentials());

        try {
            if (!CryptoUtils.validate(password, user.passwordHash(), user.passwordSalt()))
                LoginController.throwIncorrectCredentials();
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Could not validate password!", e);
        }

        final String token = CryptoUtils.genToken();

        final Session session = SessionDao.INSTANCE.insertSession(
                new SessionSkeleton.Builder()
                        .userId(user.id())
                        .duration(LoginController.SESSION_DURATION)
                        .token(token)
                        .build()
        );

        return new LoginResponse(
                email,
                user.name(),
                token,
                session.creation(),
                session.expiration()
        );
    }

    public record LoginRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("password")
            @Nullable String password
    ) implements Request {
    }

    public record LoginResponse(
            @JsonProperty("email")
            @NotNull String email,

            @JsonProperty("name")
            @NotNull String name,

            @JsonProperty("token")
            @NotNull String token,

            @JsonProperty("creation")
            @NotNull LocalDateTime creation,

            @JsonProperty("expiration")
            @NotNull LocalDateTime expiration
    ) implements Response {
        @Override
        public @NotNull String sectionName() {
            return "login";
        }
    }
}