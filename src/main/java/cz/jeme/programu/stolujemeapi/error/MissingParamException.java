package cz.jeme.programu.stolujemeapi.error;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

public class MissingParamException extends ApiException {
    public static final @NotNull String MISSING_PARAM_MESSAGE = "Missing required parameter: '%s'";
    private final @NotNull String paramName;

    public MissingParamException(final @NotNull String paramName) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorType.MISSING_PARAMETER,
                MissingParamException.MISSING_PARAM_MESSAGE.formatted(paramName)
        );
        this.paramName = paramName;
    }

    public @NotNull String getParamName() {
        return paramName;
    }
}