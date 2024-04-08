package cz.jeme.programu.stolujemeapi.error.control;

import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.error.ApiException;
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
        final HttpStatusCode status = exception.getStatusCode();
        return new ResponseEntity<>(
                ApiErrorResponse.builder()
                        .message(exception.getMessage())
                        .type(exception.getErrorType())
                        .status(status)
                        .build(),
                status
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public @NotNull ResponseEntity<ApiErrorResponse> handleNotReadable(final @NotNull HttpServletRequest request,
                                                                       final @NotNull HttpMessageNotReadableException exception) {
        return new ResponseEntity<>(
                ApiErrorResponse.builder()
                        .message("Request body is not readable")
                        .status(HttpStatus.BAD_REQUEST)
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public @NotNull ResponseEntity<ApiErrorResponse> handleOther(final @NotNull HttpServletRequest request,
                                                                 final @NotNull Exception exception) {
        final HttpStatusCode status;
        if (exception instanceof final ErrorResponse response) {
            status = response.getStatusCode();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            Stolujeme.logger().error(exception.getMessage(), exception);
        }
        final String message = status.value() >= 500 && status.value() < 600
                ? "An internal server error occurred!"
                : exception.getMessage();
        return new ResponseEntity<>(
                ApiErrorResponse.builder()
                        .message(message)
                        .status(status)
                        .build(),
                status
        );
    }
}