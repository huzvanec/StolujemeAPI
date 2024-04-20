package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.rating.Rating;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingSkeleton;
import cz.jeme.programu.stolujemeapi.db.session.Session;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public final class RatingController {
    private RatingController() {
    }

    public @NotNull ApiErrorType ratingValid(final int rating) {
        if (rating < 0 || rating > 10)
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
        final UUID mealUuid = ApiUtils.parseUuid(request.mealUuid(), "mealUuid");
        final Meal meal = MealDao.INSTANCE.mealByUuid(mealUuid)
                .orElseThrow(() -> new InvalidParamException("mealUuid", ApiErrorType.MEAL_UUID_INVALID));
        final Rating rating = RatingDao.INSTANCE.rate(new RatingSkeleton.Builder()
                .mealId(meal.id())
                .userId(session.userId())
                .rating(ratingValue)
                .build()
        );
        return ApiUtils.emptyResponse();
    }

    public record RateRequest(
            @JsonProperty("mealUuid")
            @Nullable String mealUuid,
            @JsonProperty("rating")
            @Nullable Integer rating
    ) implements Request {
    }

    public record RatingData(
            @NotNull UUID mealUuid,
            int rating,
            @NotNull LocalDateTime ratingTime
    ) {
    }

    @GetMapping("/ratings")
    @ResponseBody
    private @NotNull Response ratings() {
        final Session session = ApiUtils.authenticate();
        final Map<UUID, Integer> ratings = RatingDao.INSTANCE.ratingsByUserId(session.userId())
                .stream()
                .collect(Collectors.toMap(
                        rating -> MealDao.INSTANCE.mealById(rating.mealId())
                                .orElseThrow(() -> new RuntimeException("Could not find rating meal!"))
                                .uuid(),
                        Rating::rating
                ));

        return new RatingsResponse(
                ratings
        );
    }

    public record RatingsResponse(
            @JsonProperty("ratings")
            @NotNull Map<UUID, Integer> ratings
    ) implements Response {
    }
}
