package cz.jeme.programu.stolujemeapi.rest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.error.control.ApiErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public final class ResponseBodyEditor implements ResponseBodyAdvice<Object> {
    private ResponseBodyEditor() {
    }

    @Override
    public boolean supports(final @NotNull MethodParameter returnType,
                            final @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public @Nullable Object beforeBodyWrite(final @Nullable Object body,
                                            final @NotNull MethodParameter returnType,
                                            final @NotNull MediaType selectedContentType,
                                            final @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                            final @NotNull ServerHttpRequest request,
                                            final @NotNull ServerHttpResponse response) {
        if (!(body instanceof final Response responseBody)) return body;
        final String endpoint = request.getURI().getPath();
        return responseBody.sectionName() == null
                ? new EmptyWrapper(responseBody, endpoint)
                : new ResponseWrapper(responseBody, endpoint);
    }

    private static class EmptyWrapper {
        @JsonProperty("success")
        protected final boolean success;

        @JsonProperty("endpoint")
        protected final @NotNull String endpoint;

        @JsonProperty("timestamp")
        protected final @NotNull LocalDateTime timestamp;

        public EmptyWrapper(final @NotNull Response response,
                            final @NotNull String endpoint) {
            this.endpoint = endpoint;
            success = !(response instanceof ApiErrorResponse);
            timestamp = LocalDateTime.now();
        }
    }


    private static class ResponseWrapper extends EmptyWrapper {
        protected final @NotNull Map<String, Response> data;

        public ResponseWrapper(final @NotNull Response response,
                               final @NotNull String endpoint) {
            super(response, endpoint);
            this.data = Map.of(
                    Objects.requireNonNull(response.sectionName(), "This response does not have a section name!"),
                    response
            );
        }

        @JsonAnyGetter
        private @NotNull Map<String, Response> getData() {
            return data;
        }
    }
}