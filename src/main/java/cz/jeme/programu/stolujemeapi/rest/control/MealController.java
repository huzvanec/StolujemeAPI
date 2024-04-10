package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Canteen;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public final class MealController {
    private MealController() {
    }

    @GetMapping("/meal")
    @ResponseBody
    private @NotNull Response meal(final @NotNull @RequestBody MealRequest request) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(ApiUtils.require(request.uuid, "uuid"));
        } catch (final IllegalArgumentException e) {
            throw new InvalidParamException("uuid", ApiErrorType.UUID_CONTENTS_INVALID);
        }
        final Meal meal = MealDao.INSTANCE.mealByUuid(uuid)
                .orElseThrow(() -> new InvalidParamException("uuid", ApiErrorType.MEAL_UUID_INVALID));
        return new MealResponse(
                uuid,
                meal.canteen(),
                meal.course()
        );
    }

    public record MealRequest(
            @JsonProperty("uuid")
            @Nullable String uuid
    ) implements Request {
    }

    public record MealResponse(
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("canteen")
            @NotNull Canteen canteen,
            @JsonProperty("course")
            @NotNull Meal.Course course
    ) implements Response {
        @Override
        public @NotNull String sectionName() {
            return "meal";
        }
    }
}
