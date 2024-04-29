package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.meal.MenuEntry;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingSkeleton;
import cz.jeme.programu.stolujemeapi.db.user.Session;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public final class RatingController {
    private RatingController() {
    }

    public @NotNull ApiErrorType ratingValid(final int rating) {
        if (rating < 1 || rating > 10)
            return ApiErrorType.RATING_INVALID;
        return ApiErrorType.OK;
    }

    @PostMapping("/rate")
    @ResponseBody
    private @NotNull Response rate(final @NotNull @RequestBody RateRequest request) {
        final Session session = ApiUtils.authenticate();
        final int ratingValue = ApiUtils.validate(request.rating(),
                "rating",
                this::ratingValid
        );
        final UUID menuUuid = ApiUtils.parseUuid(request.menuUuid(), "menuUuid");
        final MenuEntry menuEntry = MealDao.INSTANCE.menuEntryByUuid(menuUuid)
                .orElseThrow(() -> new InvalidParamException("menuUuid", ApiErrorType.MENU_UUID_INVALID));
        RatingDao.INSTANCE.rate(new RatingSkeleton.Builder()
                .mealId(menuEntry.meal().id())
                .menuId(menuEntry.id())
                .userId(session.userId())
                .rating(ratingValue)
                .build()
        );
        return ApiUtils.emptyResponse();
    }

    public record RateRequest(
            @JsonProperty("menuUuid")
            @Nullable String menuUuid,
            @JsonProperty("rating")
            @Nullable Integer rating
    ) implements Request {
    }

    @GetMapping("/ratings")
    @ResponseBody
    private @NotNull Response ratings() {
        final Session session = ApiUtils.authenticate();
        return new RatingsResponse(
                RatingDao.INSTANCE.ratingsByUserId(session.userId())
        );
    }

    public record RatingsResponse(
            @JsonProperty("ratings")
            @NotNull Map<UUID, Double> ratings
    ) implements Response {
    }
}
