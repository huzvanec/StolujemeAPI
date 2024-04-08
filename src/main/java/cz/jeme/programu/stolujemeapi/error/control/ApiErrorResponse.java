package cz.jeme.programu.stolujemeapi.error.control;

import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatusCode;

public interface ApiErrorResponse extends Response {
    static @NotNull Builder builder() {
        return new ApiErrorResponseImpl.BuilderImpl();
    }

    @NotNull
    HttpStatusCode status();

    int code();

    @NotNull
    ApiErrorType type();

    @NotNull
    String message();

    interface Builder {
        @NotNull
        ApiErrorResponse build();

        @NotNull
        Builder status(final @NotNull HttpStatusCode status);

        @NotNull
        Builder type(final @NotNull ApiErrorType type);

        @NotNull
        Builder message(final @NotNull String message);
    }
}
