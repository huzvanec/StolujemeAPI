package cz.jeme.programu.stolujemeapi.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.rest.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public final class ApiErrorController implements ErrorController {
    private ApiErrorController() {
    }

    @ExceptionHandler(ApiException.class)
    public @NotNull ResponseEntity<ApiErrorResponse> handleApi(final @NotNull HttpServletRequest request,
                                                               final @NotNull ApiException exception) {
        HttpStatusCode status = exception.getStatusCode();
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        exception.getMessage(),
                        exception.getErrorType(),
                        status
                ),
                status
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public @NotNull ResponseEntity<ApiErrorResponse> handleNotReadable(final @NotNull HttpServletRequest request,
                                                                       final @NotNull HttpMessageNotReadableException exception) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        "Request body is not readable",
                        ApiErrorType.UNKNOWN,
                        HttpStatus.BAD_REQUEST
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public @NotNull ResponseEntity<ApiErrorResponse> handleOther(final @NotNull HttpServletRequest request,
                                                                 final @NotNull Exception exception) {
        final HttpStatusCode status;
        if (exception instanceof ErrorResponse response) {
            status = response.getStatusCode();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            Stolujeme.getLogger().error(exception.getMessage(), exception);
        }
        String message = status.value() >= 500 && status.value() < 600
                ? "An internal server error occurred!"
                : exception.getMessage();
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        message,
                        ApiErrorType.UNKNOWN,
                        status
                ),
                status
        );
    }

    public static class ApiErrorResponse implements Response {
        @JsonProperty("http")
        private final @NotNull HttpStatusCode status;

        @JsonProperty("httpCode")
        private final int code;

        @JsonProperty("type")
        private final @NotNull ApiErrorType type;

        @JsonProperty("message")
        private final @NotNull String message;

        public ApiErrorResponse(final @NotNull String message,
                                final @NotNull ApiErrorType type,
                                final @NotNull HttpStatusCode status) {
            int code = status.value();
            this.status = status;
            this.code = code;
            this.type = type;
            this.message = message;
        }

        @Override
        public @NotNull String getSectionName() {
            return "error";
        }

        @Override
        public @NotNull String toString() {
            return "ApiErrorResponse{" +
                   "status=" + status +
                   ", code=" + code +
                   ", type=" + type +
                   ", message='" + message + '\'' +
                   '}';
        }


        public @NotNull HttpStatusCode getStatus() {
            return status;
        }

        public int getCode() {
            return code;
        }

        public @NotNull ApiErrorType getType() {
            return type;
        }

        public @NotNull String getMessage() {
            return message;
        }
    }
}