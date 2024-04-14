package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Canteen;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.photo.Photo;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
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

import java.util.List;
import java.util.UUID;

@RestController
public final class MealController {
    private MealController() {
    }

    @GetMapping("/meal")
    @ResponseBody
    private @NotNull Response meal(final @NotNull @RequestBody MealRequest request) {
        ApiUtils.authenticate();
        final UUID uuid;
        try {
            uuid = UUID.fromString(ApiUtils.require(request.mealUuid, "mealUuid"));
        } catch (final IllegalArgumentException e) {
            throw new InvalidParamException("mealUuid", ApiErrorType.UUID_CONTENTS_INVALID);
        }
        final Meal meal = MealDao.INSTANCE.mealByUuid(uuid)
                .orElseThrow(() -> new InvalidParamException("mealUuid", ApiErrorType.MEAL_UUID_INVALID));

        final List<UUID> photoUuids = PhotoDao.INSTANCE.photoByMealId(meal.id()).stream()
                .map(Photo::uuid)
                .toList();

        return new MealResponse(
                uuid,
                meal.canteen(),
                meal.course(),
                photoUuids
        );
    }

    public record MealRequest(
            @JsonProperty("mealUuid")
            @Nullable String mealUuid
    ) implements Request {
    }

    public record MealResponse(
            @JsonProperty("mealUuid")
            @NotNull UUID mealUuid,
            @JsonProperty("canteen")
            @NotNull Canteen canteen,
            @JsonProperty("course")
            @NotNull Meal.Course course,
            @JsonProperty("photos")
            @NotNull List<UUID> photos
    ) implements Response {
        @Override
        public @NotNull String sectionName() {
            return "meal";
        }
    }
}
