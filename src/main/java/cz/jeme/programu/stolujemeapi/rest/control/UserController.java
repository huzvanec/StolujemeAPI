package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.EnvVar;
import cz.jeme.programu.stolujemeapi.Lang;
import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.user.*;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.apache.commons.validator.routines.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public final class UserController {
    public static final int NAME_LENGTH_MIN = 3;
    public static final int NAME_LENGTH_MAX = 30;
    // language=regexp
    public static final @NotNull String NAME_REGEX = "^[a-zA-Z0-9 ._-]+$";
    public static final int PASSWORD_LENGTH_MIN = 5;
    public static final int PASSWORD_LENGTH_MAX = 100;
    public static final int EMAIL_LENGTH_MAX = 250;


    public static final @NotNull Duration REGISTRATION_DURATION = Duration.ofMinutes(15);

    public static final @NotNull String CREDENTIALS_PLACEHOLDER = "#credentials";
    public static final @NotNull Duration SESSION_DURATION = Duration.ofDays(30);
    private static final @NotNull InvalidParamException INVALID_CREDENTIALS = new InvalidParamException(UserController.CREDENTIALS_PLACEHOLDER, ApiErrorType.INVALID_CREDENTIALS);

    private final @NotNull UserDao userDao = UserDao.INSTANCE;
    private final @NotNull String emailUsername = EnvVar.EMAIL_USERNAME.require();
    private final @NotNull JavaMailSender emailSender;

    @Autowired
    private UserController(final @NotNull JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    private @NotNull Response register(final @NotNull @RequestBody RegisterRequest request) {
        final String password = ApiUtils.validate(
                request.password(),
                "password",
                psw -> {
                    final int length = psw.length();
                    if (length < UserController.PASSWORD_LENGTH_MIN ||
                        length > UserController.PASSWORD_LENGTH_MAX)
                        return ApiErrorType.PASSWORD_LENGTH_INVALID;
                    return ApiErrorType.OK;
                }
        );

        final Lang language;
        try {
            language = request.language() == null
                    ? Lang.EN // language default fallback to english
                    : Lang.valueOf(request.language().toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new InvalidParamException("language", ApiErrorType.LANGUAGE_INVALID);
        }

        final String email = ApiUtils.validate(
                request.email(),
                "email",
                mail -> {
                    if (mail.length() > UserController.EMAIL_LENGTH_MAX)
                        return ApiErrorType.EMAIL_LENGTH_INVALID;
                    if (!EmailValidator.getInstance().isValid(mail))
                        return ApiErrorType.EMAIL_CONTENTS_INVALID;
                    if (userDao.existsUserEmail(mail)) // a verified user with this email exists
                        return ApiErrorType.EMAIL_NOT_UNIQUE;
                    return ApiErrorType.OK;
                }
        );

        final Canteen canteen;
        try {
            canteen = Canteen.fromEmail(email);
        } catch (final IllegalArgumentException e) {
            throw new InvalidParamException("email", ApiErrorType.EMAIL_CANTEEN_INVALID);
        }

        final AtomicReference<Optional<Registration>> atomicReg = new AtomicReference<>();

        final String name = ApiUtils.validate(
                request.name(),
                "name",
                nme -> {
                    final int length = nme.length();
                    if (length < UserController.NAME_LENGTH_MIN ||
                        length > UserController.NAME_LENGTH_MAX)
                        return ApiErrorType.NAME_LENGTH_INVALID;
                    if (!nme.matches(UserController.NAME_REGEX)) return ApiErrorType.NAME_CONTENTS_INVALID;
                    if (userDao.existsUserName(nme)) // a verified user with this name exists
                        return ApiErrorType.NAME_NOT_UNIQUE;

                    final Optional<Registration> oReg = userDao.activeRegistrationByEmailAndName(email, nme);
                    atomicReg.set(oReg);
                    if (oReg.isEmpty()) // no active registration with this email and name exists
                        return ApiErrorType.OK;
                    final Registration registration = oReg.get();
                    if (CryptoUtils.validate(password, registration.passwordHash(), registration.passwordSalt())) {
                        // the email, name and password all match = this is a verification resend request
                        return ApiErrorType.OK;
                    }
                    // the password does not match the ongoing verification, reserve it until the verification expires
                    return ApiErrorType.NAME_NOT_UNIQUE;
                }
        );

        // use existing salt and hash if an active verification exists
        final Optional<Registration> oReg = atomicReg.get();
        final String salt = oReg
                .map(Registration::passwordSalt)
                .orElseGet(CryptoUtils::randomSalt);
        final String hash = oReg
                .map(Registration::passwordHash)
                .orElse(CryptoUtils.hash(password, salt));

        final String code = CryptoUtils.randomVerification();

        // create registration
        final Registration registration = userDao.insertRegistration(new RegistrationSkeleton.Builder()
                .email(email)
                .name(name)
                .canteen(canteen)
                .passwordHash(hash)
                .passwordSalt(salt)
                .code(code)
                .duration(UserController.REGISTRATION_DURATION)
                .build()
        );

        // send email
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Stolujeme <%s>".formatted(emailUsername));
        message.setTo(email);
        message.setSubject(language.verificationSubject());
        message.setText(language.verification()
                .replace("${EMAIL}", email)
                .replace("${NAME}", name)
                .replace("${CODE}", UriUtils.encodePathSegment(code, StandardCharsets.UTF_8))
        );
        // TODO
        emailSender.send(message);

        return new RegisterResponse(new RegistrationData(registration));
    }

    public record RegisterRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("name")
            @Nullable String name,

            @JsonProperty("password")
            @Nullable String password,

            @JsonProperty("language")
            @Nullable String language
    ) implements Request {
    }

    public record RegisterResponse(
            @JsonProperty("registration")
            @NotNull RegistrationData registrationData
    ) implements Response {
    }

    @PostMapping("/auth/verify")
    @ResponseBody
    private @NotNull Response verify(final @NotNull @RequestBody VerifyRequest request) {
        final String code = ApiUtils.require(request.code(), "code");


        final Registration verification = userDao.registrationByCode(code)
                .orElseThrow(() -> new InvalidParamException("code", ApiErrorType.VERIFICATION_CODE_INVALID));

        if (verification.expired())
            throw new InvalidParamException("code", ApiErrorType.VERIFICATION_EXPIRED);

        userDao.register(verification);
        return ApiUtils.emptyResponse();
    }

    public record VerifyRequest(
            @JsonProperty("code")
            @Nullable String code
    ) implements Request {
    }

    @PostMapping("/auth/log-in")
    @ResponseBody
    private @NotNull Response logIn(final @NotNull @RequestBody LoginRequest request) {
        final String email = ApiUtils.require(
                request.email(),
                "email"
        );

        final String password = ApiUtils.require(
                request.password(),
                "password"
        );

        final Optional<User> oUser = userDao.userByEmail(email);
        if (oUser.isEmpty()) {
            // hash to create fake delay so that an attacker does not know whether they guessed an email
            CryptoUtils.hash(password, CryptoUtils.randomSalt());
            throw UserController.INVALID_CREDENTIALS;
        }

        final User user = oUser.get();

        if (!CryptoUtils.validate(password, user.passwordHash(), user.passwordSalt()))
            throw UserController.INVALID_CREDENTIALS;

        final String token = CryptoUtils.randomSession();

        final Session session = userDao.insertSession(
                new SessionSkeleton.Builder()
                        .userId(user.id())
                        .duration(UserController.SESSION_DURATION)
                        .token(token)
                        .build()
        );

        return new LoginResponse(new SessionData(session));
    }

    public record LoginRequest(
            @JsonProperty("email")
            @Nullable String email,

            @JsonProperty("password")
            @Nullable String password
    ) implements Request {
    }

    public record LoginResponse(
            @JsonProperty("session")
            @NotNull SessionData sessionData
    ) implements Response {
    }

    @PostMapping("/auth/log-out")
    @ResponseBody
    private @NotNull Response logout() {
        final Session session = ApiUtils.authenticate();
        if (!userDao.endSession(session.id()))
            throw new RuntimeException("Session could not be ended!");
        return ApiUtils.emptyResponse();
    }

    public record RegistrationData(
            @JsonProperty("creationTime")
            @NotNull LocalDateTime creationTime,
            @JsonProperty("expirationTime")
            @NotNull LocalDateTime expirationTime
    ) {
        public RegistrationData(final @NotNull Registration registration) {
            this(registration.creationTime(), registration.expirationTime());
        }
    }

    public record SessionData(
            @JsonProperty("token")
            @NotNull String token,
            @JsonProperty("creationTime")
            @NotNull LocalDateTime creationTime,
            @JsonProperty("expirationTime")
            @NotNull LocalDateTime expirationTime
    ) {
        public SessionData(final @NotNull Session session) {
            this(session.token(), session.creationTime(), session.expirationTime());
        }
    }

    @GetMapping("/auth/session")
    @ResponseBody
    private @NotNull Response testAuth() {
        return new LoginResponse(new SessionData(ApiUtils.authenticate()));
    }
}