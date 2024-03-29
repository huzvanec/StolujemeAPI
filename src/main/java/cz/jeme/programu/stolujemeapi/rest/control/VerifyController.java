package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.db.verification.Verification;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.RequestUtils;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class VerifyController {
    private VerifyController() {
    }

    @PostMapping("/verify")
    @ResponseBody
    public @NotNull Response verify(final @NotNull @RequestBody VerifyRequest request) {
        String code = RequestUtils.require(request.code(), "code");


        Verification verification = Stolujeme.getDatabase().getVerificationDao().getByCode(code)
                .orElseThrow(() -> new InvalidParamException("code", ApiErrorType.VERIFICATION_CODE_INVALID));

        if (verification.expired())
            throw new InvalidParamException("code", ApiErrorType.VERIFICATION_EXPIRED);

        Stolujeme.getDatabase().getVerificationDao().verify(verification);

        return Response.EMPTY;
    }

    public record VerifyRequest(
            @JsonProperty("code")
            @Nullable String code
    ) {
    }
}