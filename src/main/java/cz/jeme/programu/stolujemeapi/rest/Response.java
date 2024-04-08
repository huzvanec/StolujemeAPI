package cz.jeme.programu.stolujemeapi.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
public interface Response {
    @JsonIgnore
    @Nullable
    String sectionName();
}