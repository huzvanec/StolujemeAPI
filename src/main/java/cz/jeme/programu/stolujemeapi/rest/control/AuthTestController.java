package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.session.Session;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public final class AuthTestController {
    private AuthTestController() {
    }

    @PostMapping("/test-auth")
    @ResponseBody
    public @NotNull Response testAuth() {
        final Session session = ApiUtils.authenticate();
        final User user = UserDao.dao().byId(session.userId())
                .orElseThrow(() -> new RuntimeException("Session user id does not correspond to any users!"));
        return new AuthTestResponse(
                user.email(),
                user.name(),
                session.creation(),
                session.expiration()
        );
    }

    public record AuthTestResponse(
            @JsonProperty("email")
            @NotNull String email,
            @JsonProperty("name")
            @NotNull String name,
            @JsonProperty("creation")
            @NotNull LocalDateTime creation,
            @JsonProperty("expiration")
            @NotNull LocalDateTime expiration
    ) implements Response {
        @Override
        public @NotNull String sectionName() {
            return "session";
        }
    }
}