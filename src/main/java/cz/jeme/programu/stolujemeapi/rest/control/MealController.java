package cz.jeme.programu.stolujemeapi.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.meal.MealName;
import cz.jeme.programu.stolujemeapi.db.meal.MenuEntry;
import cz.jeme.programu.stolujemeapi.db.photo.Photo;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.user.Session;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/meal/{uuid}")
    @ResponseBody
    private @NotNull Response meal(final @NotNull @PathVariable("uuid") String mealUuid) {
        ApiUtils.authenticate();
        final UUID uuid = ApiUtils.parseUuid(mealUuid, "uuid");
        final Meal meal = MealDao.INSTANCE.mealByUuid(uuid)
                .orElseThrow(() -> new InvalidParamException("uuid", ApiErrorType.MEAL_UUID_INVALID));

        final List<UUID> photoUuids = PhotoDao.INSTANCE.photoByMealId(meal.id()).stream()
                .map(Photo::uuid)
                .toList();

        final List<String> mealNames = MealDao.INSTANCE.mealNamesByMealId(meal.id()).stream()
                .map(MealName::name)
                .toList();

        return new MealResponse(
                new MealData(meal),
                mealNames,
                photoUuids
        );
    }

    public record MealResponse(
            @JsonProperty("meal")
            @NotNull MealData mealData,
            @JsonProperty("names")
            @NotNull List<String> names,
            @JsonProperty("photos")
            @NotNull List<UUID> photos
    ) implements Response {
    }

    public record MealData(
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("canteen")
            @NotNull String canteen,
            @JsonProperty("course")
            @NotNull Meal.Course course,
            @JsonProperty("description")
            @Nullable String description
    ) {
        public MealData(final @NotNull Meal meal) {
            this(meal.uuid(), meal.canteen().name(), meal.course(), meal.description());
        }
    }

    @GetMapping("/menu")
    @ResponseBody
    private @NotNull Response menu(
            final @Nullable @RequestParam(name = "from-date", required = false) String fromDateStr,
            final @Nullable @RequestParam(name = "to-date", required = false) String toDateStr) {
        final Session session = ApiUtils.authenticate();
        final LocalDate fromDate = fromDateStr == null
                ? LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                : parseDate(fromDateStr, "from-date");

        final LocalDate toDate = toDateStr == null
                ? LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                : parseDate(toDateStr, "to-date");

        if (fromDate.isAfter(toDate))
            throw new InvalidParamException("from-date", ApiErrorType.DATE_ORDER_INVALID);

        final Canteen canteen = UserDao.INSTANCE.userBySession(session).canteen();

        final Map<Integer, Double> userRatings = RatingDao.INSTANCE.ratingsByDates(
                fromDate,
                toDate,
                RatingDao.RatingRequestType.USER,
                session.userId()
        );

        final Map<Integer, Double> globalRatings = RatingDao.INSTANCE.ratingsByDates(
                fromDate,
                toDate,
                RatingDao.RatingRequestType.GLOBAL,
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
                                                        globalRatings.get(menuEntry.meal().id())
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
            @JsonProperty("global")
            @Nullable Double globalRating
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
