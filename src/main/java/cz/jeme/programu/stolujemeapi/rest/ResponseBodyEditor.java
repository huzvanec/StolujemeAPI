package cz.jeme.programu.stolujemeapi.rest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.error.ApiErrorController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.ZonedDateTime;
import java.util.Map;

@ControllerAdvice
public final class ResponseBodyEditor implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(final @NotNull MethodParameter returnType,
                            final @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public @NotNull Object beforeBodyWrite(final @Nullable Object body,
                                           final @NotNull MethodParameter returnType,
                                           final @NotNull MediaType selectedContentType,
                                           final @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                           final @NotNull ServerHttpRequest request,
                                           final @NotNull ServerHttpResponse response) {
        if (!(body instanceof Response responseBody)) return body;
        String endpoint = request.getURI().getPath();
        return responseBody.getSectionName() == null
                ? new EmptyWrapper(body, endpoint)
                : new ResponseWrapper(responseBody, endpoint);
    }

    private static class EmptyWrapper {
        @JsonProperty("success")
        protected final boolean success;

        @JsonProperty("endpoint")
        protected final @NotNull String endpoint;

        @JsonProperty("timestamp")
        protected final @NotNull ZonedDateTime timestamp;

        public EmptyWrapper(final @NotNull Object body,
                            final @NotNull String endpoint) {
            this.endpoint = endpoint;
            success = !(body instanceof ApiErrorController.ApiErrorResponse);
            timestamp = ZonedDateTime.now();
        }
    }


    private static class ResponseWrapper extends EmptyWrapper {
        protected final @NotNull Map<String, Response> data;

        public ResponseWrapper(final @NotNull Response response,
                               final @NotNull String endpoint) {
            super(response, endpoint);
            if (response.getSectionName() == null)
                throw new IllegalArgumentException("This response does not have a section name!");
            this.data = Map.of(response.getSectionName(), response);
        }

        @JsonAnyGetter
        private @NotNull Map<String, Response> getData() {
            return data;
        }
    }
}