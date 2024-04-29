package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.meal.MenuEntry;
import cz.jeme.programu.stolujemeapi.db.photo.Photo;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.user.Session;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public final class MealController {
    private MealController() {
    }

    @GetMapping("/meal")
    @ResponseBody
    private @NotNull Response meal(final @NotNull @RequestBody MealRequest request) {
        ApiUtils.authenticate();
        final UUID uuid = ApiUtils.parseUuid(request.mealUuid(), "mealUuid");
        final Meal meal = MealDao.INSTANCE.mealByUuid(uuid)
                .orElseThrow(() -> new InvalidParamException("mealUuid", ApiErrorType.MEAL_UUID_INVALID));

        final List<UUID> photoUuids = PhotoDao.INSTANCE.photoByMealId(meal.id()).stream()
                .map(Photo::uuid)
                .toList();

        return new MealResponse(
                new MealData(meal),
                photoUuids
        );
    }

    public record MealRequest(
            @JsonProperty("mealUuid")
            @Nullable String mealUuid
    ) implements Request {
    }

    public record MealResponse(
            @JsonProperty("meal")
            @NotNull MealData mealData,
            @JsonProperty("photos")
            @NotNull List<UUID> photos
    ) implements Response {
    }

    public record MealData(
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("canteen")
            @NotNull Canteen canteen,
            @JsonProperty("course")
            @NotNull Meal.Course course,
            @JsonProperty("description")
            @Nullable String description
    ) {
        public MealData(final @NotNull Meal meal) {
            this(meal.uuid(), meal.canteen(), meal.course(), meal.description());
        }
    }

    @GetMapping("/menu")
    @ResponseBody
    private @NotNull Response menu(final @NotNull @RequestBody MenuRequest request) {
        final Session session = ApiUtils.authenticate();
        final LocalDate fromDate = request.fromDate() == null
                ? LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                : parseDate(request.fromDate(), "fromDate");

        final LocalDate toDate = request.toDate() == null
                ? LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                : parseDate(request.toDate(), "toDate");

        if (fromDate.isAfter(toDate))
            throw new InvalidParamException("fromDate", ApiErrorType.DATE_ORDER_INVALID);

        final Canteen canteen = UserDao.INSTANCE.userBySession(session).canteen();

        final Map<Integer, Double> userRatings = RatingDao.INSTANCE.ratingsByDates(
                fromDate,
                toDate,
                RatingDao.RatingRequestType.USER,
                session.userId()
        );

        final Map<Integer, Double> othersRatings = RatingDao.INSTANCE.ratingsByDates(
                fromDate,
                toDate,
                RatingDao.RatingRequestType.OTHERS,
                session.userId()
        );

        final Map<LocalDate, List<MenuEntryData>> entries = MealDao.INSTANCE.menuEntriesByDates(canteen, fromDate, toDate)
                .stream()
                .collect(Collectors.groupingBy(
                        MenuEntry::date,
                        TreeMap::new,
                        Collectors.mapping(
                                menuEntry -> new MenuEntryData(
                                        menuEntry.mealName(),
                                        menuEntry.courseNumber(),
                                        menuEntry.uuid(),
                                        new MenuMealData(
                                                menuEntry.meal(),
                                                new MenuMealRatingData(
                                                        userRatings.get(menuEntry.meal().id()),
                                                        othersRatings.get(menuEntry.meal().id())
                                                )
                                        )
                                ),
                                Collectors.toList()
                        )
                ));
        entries.values().forEach(list -> list.sort(MenuEntryDataComparator.INSTANCE));

        return new MenuResponse(canteen.name(), entries);
    }

    private @NotNull LocalDate parseDate(final @NotNull String dateStr, final @NotNull String paramName) {
        try {
            return LocalDate.parse(dateStr);
        } catch (final DateTimeParseException e) {
            throw new InvalidParamException(paramName, ApiErrorType.DATE_CONTENTS_INVALID);
        }
    }

    public record MenuRequest(
            @JsonProperty("fromDate")
            @Nullable String fromDate,
            @JsonProperty("toDate")
            @Nullable String toDate
    ) implements Request {
    }

    public record MenuResponse(
            @JsonProperty("canteen")
            @NotNull String canteen,
            @JsonProperty("menu")
            @NotNull Map<LocalDate, List<MenuEntryData>> menuEntries
    ) implements Response {
    }

    public record MenuEntryData(
            @JsonProperty("name")
            @NotNull String mealName,
            @JsonProperty("courseNumber")
            @Nullable Integer courseNumber,
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("meal")
            @NotNull MenuMealData mealData
    ) {
    }

    public record MenuMealData(
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("course")
            @NotNull Meal.Course course,
            @JsonProperty("ratings")
            @NotNull MenuMealRatingData ratingData,
            @JsonProperty("description")
            @Nullable String description
    ) {
        public MenuMealData(final @NotNull Meal meal, final @NotNull MenuMealRatingData ratingData) {
            this(meal.uuid(), meal.course(), ratingData, meal.description());
        }
    }

    public record MenuMealRatingData(
            @JsonProperty("user")
            @Nullable Double userRating,
            @JsonProperty("others")
            @Nullable Double othersRating
    ) {
    }

    public enum MenuEntryDataComparator implements Comparator<MenuEntryData> {
        INSTANCE;

        @Override
        public int compare(final @NotNull MenuEntryData entry1, final @NotNull MenuEntryData entry2) {
            final Meal.Course course1 = entry1.mealData().course();
            final Meal.Course course2 = entry2.mealData().course();

            final Integer courseNumber1 = entry1.courseNumber();
            final Integer courseNumber2 = entry2.courseNumber();

            // soup always goes first
            if (course1 == Meal.Course.SOUP && course2 != Meal.Course.SOUP) {
                return -1;
            } else if (course1 != Meal.Course.SOUP && course2 == Meal.Course.SOUP) {
                return 1;
            }

            // compare main courses by course numbers
            // course numbers are always nonnull
            if (course1 == Meal.Course.MAIN && course2 == Meal.Course.MAIN) {
                return Integer.compare(
                        Objects.requireNonNull(courseNumber1),
                        Objects.requireNonNull(courseNumber2)
                );
            }

            // addition always goes last
            if (course1 == Meal.Course.ADDITION && course2 != Meal.Course.ADDITION) {
                return 1;
            } else if (course1 != Meal.Course.ADDITION && course2 == Meal.Course.ADDITION) {
                return -1;
            }

            return 0;
        }
    }
}
