package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.db.verification.StoluVerificationSkeleton;
import cz.jeme.programu.stolujemeapi.db.verification.Verification;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.ApiException;
import cz.jeme.programu.stolujemeapi.rest.RequestUtils;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
public final class VerificationController {
    public static final @NotNull Duration VERIFICATION_DURATION = Duration.ofMinutes(15);

    private VerificationController() {
    }

    @PostMapping("/verification")
    @ResponseBody
    public @NotNull Response verification(final @NotNull @RequestBody VerificationRequest request) {
        String password = RequestUtils.require(
                request.password(),
                "password"
        );


        String email = RequestUtils.require(
                request.email(),
                "email"
        );

        User user = Stolujeme.getDatabase().getUserDao().getByEmail(email)
                .orElseThrow(LoginController.supplyIncorrectCredentials());


        try {
            if (!CryptoUtils.validate(password, user.passwordHash(), user.passwordSalt()))
                LoginController.throwIncorrectCredentials();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Could not validate password!", e);
        }

        if (user.verified())
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorType.USER_ALREADY_VERIFIED);

        String code = CryptoUtils.genVerification();

        Verification verification = Stolujeme.getDatabase().getVerificationDao().insert(
                new StoluVerificationSkeleton(user.id(), VERIFICATION_DURATION, code)
        );

        return new VerificationResponse(
                email,
                user.name(),
                ZonedDateTime.of(verification.creation(), ZoneId.systemDefault()),
                ZonedDateTime.of(verification.expiration(), ZoneId.systemDefault())
        );
    }

    public record VerificationRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("password")
            @Nullable String password
    ) {
    }

    public record VerificationResponse(
            @JsonProperty("email")
            @NotNull String email,

            @JsonProperty("name")
            @NotNull String name,

            @JsonProperty("creation")
            @NotNull ZonedDateTime creation,


            @JsonProperty("expiration")
            @NotNull ZonedDateTime expiration
    ) implements Response {
        @Override
        public @NotNull String getSectionName() {
            return "verification";
        }
    }
}