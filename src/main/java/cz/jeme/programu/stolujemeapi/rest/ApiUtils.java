package cz.jeme.programu.stolujemeapi.rest;

import cz.jeme.programu.stolujemeapi.db.CryptoUtils;
import cz.jeme.programu.stolujemeapi.db.session.Session;
import cz.jeme.programu.stolujemeapi.db.session.SessionDao;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.ApiException;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.error.MissingParamException;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Function;

public final class ApiUtils {
    private static final @NotNull Response EMPTY_RESPONSE = new Response() {
        @Override
        public @Nullable String sectionName() {
            return null;
        }
    };

    private static final @NotNull ApiException INVALID_AUTH = new ApiException(HttpStatus.UNAUTHORIZED, "Invalid authentication!");

    private ApiUtils() {
        throw new AssertionError();
    }


    public static <T> @NotNull T require(final @Nullable T param, final @NotNull String name) {
        if (param == null) throw new MissingParamException(name); // api
        return param;
    }

    public static <T> @NotNull T validate(final @Nullable T param,
                                          final @NotNull String name,
                                          final @NotNull Function<@NotNull T, @NotNull ApiErrorType> validation) {
        ApiUtils.require(param, name);
        final ApiErrorType type = validation.apply(param);
        if (type == ApiErrorType.OK) return param;
        if (type == ApiErrorType.MISSING_PARAMETER)
            throw new IllegalArgumentException("Validation returned %s when parameter not missing!"
                    .formatted(ApiErrorType.MISSING_PARAMETER.name()));
        throw new InvalidParamException( // api
                name,
                type
        );
    }

    public static @NotNull Session authenticate() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null)
            throw new RuntimeException("No request attributes are bound to this thread!");

        final HttpServletRequest servletRequest = ((ServletRequestAttributes) attributes).getRequest();
        String token = servletRequest.getHeader("Authorization");
        if (token == null)
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing authentication (bearer token)!"
            );
        if (!token.startsWith("Bearer "))
            throw ApiUtils.INVALID_AUTH;
        token = token.substring(7);
        if (token.length() != CryptoUtils.TOKEN_LENGTH_BASE64)
            throw ApiUtils.INVALID_AUTH;
        final Session session = SessionDao.INSTANCE.sessionByToken(token)
                .orElseThrow(() -> ApiUtils.INVALID_AUTH);
        if (session.expired()) throw ApiUtils.INVALID_AUTH;
        return session;
    }

    public static @NotNull Response emptyResponse() {
        return ApiUtils.EMPTY_RESPONSE;
    }
}