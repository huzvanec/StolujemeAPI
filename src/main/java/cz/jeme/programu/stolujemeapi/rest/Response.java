package cz.jeme.programu.stolujemeapi.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
public interface Response {
    @JsonIgnore
    @Nullable String getSectionName();

    @NotNull Response EMPTY = new Response() {
        @Override
        public @Nullable String getSectionName() {
            return null;
        }
    };
}