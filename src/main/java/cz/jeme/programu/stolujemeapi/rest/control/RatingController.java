package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.meal.MenuEntry;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingSkeleton;
import cz.jeme.programu.stolujemeapi.db.user.Session;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.ApiException;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
public final class RatingController {
    private static final int OLDEST_RATING_MENU = 7; // the oldest menu entry that can be rated (in days from current date)

    private RatingController() {
    }

    public @NotNull ApiErrorType ratingValid(final int rating) {
        if (rating < 1 || rating > 5)
            return ApiErrorType.RATING_INVALID;
        return ApiErrorType.OK;
    }

    @PutMapping("/menu/{menuUuid}/rating")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    private @NotNull Response rate(final @NotNull @RequestBody RateRequest request,
                                   final @NotNull @PathVariable("menuUuid") String menuUuidStr) {
        final Session session = ApiUtils.authenticate();
        final int ratingValue = ApiUtils.validate(request.rating(),
                "rating",
                this::ratingValid
        );
        final UUID menuUuid = ApiUtils.parseUuid(menuUuidStr, "menuUuid");
        final MenuEntry menuEntry = MealDao.INSTANCE.menuEntryByUuid(menuUuid)
                .orElseThrow(() -> new InvalidParamException("menuUuid", ApiErrorType.MENU_UUID_INVALID));
        if (ChronoUnit.DAYS.between(menuEntry.date(), LocalDate.now()) > RatingController.OLDEST_RATING_MENU)
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorType.MENU_TOO_OLD);
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
            @JsonProperty("rating")
            @Nullable Integer rating
    ) implements Request {
    }

    @GetMapping("/menu/{menuUuid}/rating")
    @ResponseBody
    private @NotNull Response rating(final @NotNull @PathVariable("menuUuid") String menuUuidStr) {
        final Session session = ApiUtils.authenticate();

        final UUID menuUuid = ApiUtils.parseUuid(menuUuidStr, "menuUuid");
        final MenuEntry menuEntry = MealDao.INSTANCE.menuEntryByUuid(menuUuid)
                .orElseThrow(() -> new InvalidParamException("menuUuid", ApiErrorType.MENU_UUID_INVALID));

        return new RatingResponse(
                RatingDao.INSTANCE.ratingsByMealId(menuEntry.meal().id(), session.userId())
        );
    }

    public record RatingResponse(
            @JsonProperty("ratings")
            @NotNull RatingDao.MealRatingData ratingData
    ) implements Response {
    }
}