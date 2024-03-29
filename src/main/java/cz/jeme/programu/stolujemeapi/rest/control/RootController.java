package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public final class RootController {
    private RootController() {
    }

    @GetMapping("/")
    @ResponseBody
    public @NotNull Response root() {
        return new RootResponse();
    }

    public static final class RootResponse implements Response {
        @JsonProperty("status")
        private final @NotNull String status = "ok";

        @JsonProperty("#")
        private final @NotNull List<String> logo = Stolujeme.getLogo();

        @Override
        public @NotNull String getSectionName() {
            return "stolujeme";
        }
    }
}