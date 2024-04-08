package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.verification.Verification;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationDao;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationSkeleton;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.ApiException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
public final class VerificationController {
    public static final @NotNull Duration VERIFICATION_DURATION = Duration.ofMinutes(15);

    private VerificationController() {
    }

    @PostMapping("/verification")
    @ResponseBody
    public @NotNull Response verification(final @NotNull @RequestBody VerificationRequest request) {
        final String password = ApiUtils.require(
                request.password(),
                "password"
        );


        final String email = ApiUtils.require(
                request.email(),
                "email"
        );

        final User user = UserDao.INSTANCE.byEmail(email)
                .orElseThrow(LoginController.supplyIncorrectCredentials());


        try {
            if (!CryptoUtils.validate(password, user.passwordHash(), user.passwordSalt()))
                LoginController.throwIncorrectCredentials();
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Could not validate password!", e);
        }

        if (user.verified())
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorType.USER_ALREADY_VERIFIED);

        final String code = CryptoUtils.genVerification();

        final Verification verification = VerificationDao.INSTANCE.insert(
                new VerificationSkeleton.Builder()
                        .userId(user.id())
                        .duration(VerificationController.VERIFICATION_DURATION)
                        .code(code)
                        .build()
        );

        return new VerificationResponse(
                email,
                user.name(),
                verification.creation(),
                verification.expiration()
        );
    }

    public record VerificationRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("password")
            @Nullable String password
    ) implements Request {
    }

    public record VerificationResponse(
            @JsonProperty("email")
            @NotNull String email,

            @JsonProperty("name")
            @NotNull String name,

            @JsonProperty("creation")
            @NotNull LocalDateTime creation,


            @JsonProperty("expiration")
            @NotNull LocalDateTime expiration
    ) implements Response {
        @Override
        public @NotNull String sectionName() {
            return "verification";
        }
    }
}