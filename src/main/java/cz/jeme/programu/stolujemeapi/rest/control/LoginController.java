package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.RequestUtils;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;
import java.util.function.Supplier;

@RestController
public final class LoginController {
    public static final @NotNull String CREDENTIALS = "#credentials";

    public static void throwIncorrectCredentials() {
        throw new InvalidParamException(CREDENTIALS, ApiErrorType.INVALID_CREDENTIALS);
    }

    public static @NotNull Supplier<InvalidParamException> supplyIncorrectCredentials() {
        return () -> new InvalidParamException(CREDENTIALS, ApiErrorType.INVALID_CREDENTIALS);
    }

    private LoginController() {
    }

    @PostMapping("/login")
    @ResponseBody
    public @NotNull Response login(final @NotNull @RequestBody LoginRequest request) {
        String email = RequestUtils.require(
                request.email(),
                "email"
        );

        String password = RequestUtils.require(
                request.password(),
                "password"
        );

        User user = Stolujeme.getDatabase().getUserDao().getByEmail(email)
                .orElseThrow(supplyIncorrectCredentials());

        try {
            if (!CryptoUtils.validate(password, user.passwordHash(), user.passwordSalt()))
                throwIncorrectCredentials();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Could not validate password!", e);
        }

        String token = CryptoUtils.genToken();


        return new LoginResponse(
                email,
                user.name(),
                token
        );
    }

    public record LoginRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("password")
            @Nullable String password
    ) {
    }

    public record LoginResponse(
            @JsonProperty("email")
            @NotNull String email,

            @JsonProperty("name")
            @NotNull String name,

            @JsonProperty("token")
            @NotNull String token
    ) implements Response {
        @Override
        public @NotNull String getSectionName() {
            return "login";
        }
    }
}