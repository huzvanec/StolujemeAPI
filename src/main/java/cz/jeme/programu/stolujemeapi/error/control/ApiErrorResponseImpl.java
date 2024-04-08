package cz.jeme.programu.stolujemeapi.error.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatusCode;

import java.util.Objects;

final class ApiErrorResponseImpl implements ApiErrorResponse {
    @JsonProperty("http")
    private final @NotNull HttpStatusCode status;

    @JsonProperty("httpCode")
    private final int code;

    @JsonProperty("type")
    private final @NotNull ApiErrorType type;

    @JsonProperty("message")
    private final @NotNull String message;

    private ApiErrorResponseImpl(final @NotNull BuilderImpl builder) {
        status = Objects.requireNonNull(builder.status, "status");
        type = builder.type;
        message = Objects.requireNonNull(builder.message, "message");
        code = status.value();
    }

    @Override
    public @NotNull String sectionName() {
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

    @Override
    public @NotNull HttpStatusCode status() {
        return status;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public @NotNull ApiErrorType type() {
        return type;
    }

    @Override
    public @NotNull String message() {
        return message;
    }

    static final class BuilderImpl implements Builder {
        private @Nullable HttpStatusCode status;
        private @NotNull ApiErrorType type = ApiErrorType.UNKNOWN;
        private @Nullable String message;


        @Override
        public @NotNull ApiErrorResponse build() {
            return new ApiErrorResponseImpl(this);
        }

        @Override
        public @NotNull Builder status(final @NotNull HttpStatusCode status) {
            this.status = status;
            return this;
        }

        @Override
        public @NotNull Builder type(final @NotNull ApiErrorType type) {
            if (type == ApiErrorType.OK)
                throw new IllegalArgumentException("\"OK\" is not a valid error type!");
            this.type = type;
            return this;
        }

        @Override
        public @NotNull Builder message(final @NotNull String message) {
            this.message = message;
            return this;
        }
    }
}