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

    @GetMapping("/meals/{mealUuid}")
    @ResponseBody
    private @NotNull Response meal(final @NotNull @PathVariable("mealUuid") String mealUuid) {
        final Session session = ApiUtils.authenticate();
        final UUID uuid = ApiUtils.parseUuid(mealUuid, "mealUuid");
        final Meal meal = MealDao.INSTANCE.mealByUuid(uuid)
                .orElseThrow(() -> new InvalidParamException("mealUuid", ApiErrorType.MEAL_UUID_INVALID));

        final List<UUID> photoUuids = PhotoDao.INSTANCE.photosByMealId(meal.id()).stream()
                .map(Photo::uuid)
                .toList();

        final List<String> mealNames = MealDao.INSTANCE.mealNamesByMealId(meal.id()).stream()
                .map(MealName::name)
                .toList();

        final RatingDao.MealRatingData ratingData = RatingDao.INSTANCE.ratingsByMealId(meal.id(), session.userId());

        return new MealResponse(new MealData(
                meal,
                ratingData,
                mealNames,
                photoUuids
        ));
    }

    public record MealResponse(
            @JsonProperty("meal")
            @NotNull MealData mealData
    ) implements Response {
    }

    public record MealData(
            @JsonProperty("canteen")
            @NotNull String canteen,
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("course")
            @NotNull Meal.Course course,
            @JsonProperty("description")
            @Nullable String description,
            @JsonProperty("ratings")
            @NotNull RatingDao.MealRatingData ratingData,
            @JsonProperty("names")
            @NotNull List<String> names,
            @JsonProperty("photos")
            @NotNull List<UUID> photos
    ) {
        public MealData(final @NotNull Meal meal,
                        final @NotNull RatingDao.MealRatingData ratingData,
                        final @NotNull List<String> names,
                        final @NotNull List<UUID> photos) {
            this(
                    meal.canteen().name(),
                    meal.uuid(),
                    meal.course(),
                    meal.description(),
                    ratingData,
                    names,
                    photos
            );
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

        final Map<Integer, Double> userRatings = RatingDao.INSTANCE.averageRatingsByDates(
                fromDate,
                toDate,
                RatingDao.RatingRequestType.USER,
                session.userId()
        );

        final Map<Integer, Double> globalRatings = RatingDao.INSTANCE.averageRatingsByDates(
                fromDate,
                toDate,
                RatingDao.RatingRequestType.GLOBAL,
                session.userId()
        );

        final Map<Integer, Integer> currentRatings = RatingDao.INSTANCE.currentRatingsByDates(
                fromDate,
                toDate,
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
                                        currentRatings.get(menuEntry.id()),
                                        new MenuMealData(
                                                menuEntry.meal(),
                                                new RatingDao.MealRatingData(
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
            @JsonProperty("currentRating")
            @Nullable Integer rating,
            @JsonProperty("meal")
            @NotNull MenuMealData mealData
    ) {
    }

    public record MenuMealData(
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("course")
            @NotNull Meal.Course course,
            @JsonProperty("description")
            @Nullable String description,
            @JsonProperty("ratings")
            @NotNull RatingDao.MealRatingData ratingData
    ) {
        public MenuMealData(final @NotNull Meal meal, final @NotNull RatingDao.MealRatingData ratingData) {
            this(meal.uuid(), meal.course(), meal.description(), ratingData);
        }
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
