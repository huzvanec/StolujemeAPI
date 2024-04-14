package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.session.Session;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.verification.Verification;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationDao;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationSkeleton;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.ApiException;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
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

    @PostMapping("/send-verification")
    @ResponseBody
    private @NotNull Response send(final @NotNull @RequestBody SendRequest request) {
        final String password = ApiUtils.require(
                request.password(),
                "password"
        );


        final String email = ApiUtils.require(
                request.email(),
                "email"
        );

        final User user = UserDao.INSTANCE.userByEmail(email)
                .orElseThrow(SessionController.supplyIncorrectCredentials());


        try {
            if (!CryptoUtils.validate(password, user.passwordHash(), user.passwordSalt()))
                SessionController.throwIncorrectCredentials();
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Could not validate password!", e);
        }

        if (user.verified())
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorType.USER_ALREADY_VERIFIED);

        final String code = CryptoUtils.genVerification();

        final Verification verification = VerificationDao.INSTANCE.insertVerification(
                new VerificationSkeleton.Builder()
                        .userId(user.id())
                        .duration(VerificationController.VERIFICATION_DURATION)
                        .code(code)
                        .build()
        );

        return new SendResponse(
                email,
                user.name(),
                verification.creation(),
                verification.expiration()
        );
    }

    public record SendRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("password")
            @Nullable String password
    ) implements Request {
    }

    public record SendResponse(
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

    @PostMapping("/verify")
    @ResponseBody
    private @NotNull Response verify(final @NotNull @RequestBody VerifyRequest request) {
        final String code = ApiUtils.require(request.code(), "code");


        final Verification verification = VerificationDao.INSTANCE.verificationByCode(code)
                .orElseThrow(() -> new InvalidParamException("code", ApiErrorType.VERIFICATION_CODE_INVALID));

        if (verification.expired())
            throw new InvalidParamException("code", ApiErrorType.VERIFICATION_EXPIRED);

        VerificationDao.INSTANCE.verify(verification);

        return ApiUtils.emptyResponse();
    }

    public record VerifyRequest(
            @JsonProperty("code")
            @Nullable String code
    ) implements Request {
    }

    @PostMapping("/auth")
    @ResponseBody
    private @NotNull Response testAuth() {
        final Session session = ApiUtils.authenticate();
        final User user = UserDao.INSTANCE.userById(session.userId())
                .orElseThrow(() -> new RuntimeException("Session user id does not correspond to any users!"));
        return new AuthTestResponse(
                user.email(),
                user.name(),
                session.creationTime(),
                session.expirationTime()
        );
    }

    public record AuthTestResponse(
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
            return "session";
        }
    }
}