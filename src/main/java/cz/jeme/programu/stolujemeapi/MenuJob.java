package cz.jeme.programu.stolujemeapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import cz.jeme.programu.stolujemeapi.db.meal.*;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import org.jetbrains.annotations.NotNull;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MenuJob implements Job {
    public static final @NotNull URI URL;
    public static final @NotNull DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    static {
        try {
            URL = new URI("https://app.strava.cz/api/jidelnicky");
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Could not create menu URI!", e);
        }
    }

    private final @NotNull RestTemplate template = new RestTemplate();
    private final @NotNull MealDao mealDao = MealDao.INSTANCE;

    @Override
    public void execute(final @NotNull JobExecutionContext context) {
        for (final Canteen canteen : Canteen.values()) {
            Stolujeme.logger().info("Requesting menu for \"%s\"".formatted(canteen.toString()));
            final String result = Objects.requireNonNull(template.postForObject(
                    MenuJob.URL,
                    ApiUtils.jsonToString(new MenuRequest(canteen.number())),
                    String.class
            ), "Menu api post did not return anything!");
            processMenu(canteen, ApiUtils.stringToJson(result).get(0));
        }
    }

    private void processMenu(final @NotNull Canteen canteen, final @NotNull JsonNode tables) {
        final List<MenuEntrySkeleton> dayEntries = new ArrayList<>();
        for (final JsonNode day : tables) {
            dayEntries.clear();
            final LocalDate date = LocalDate.parse(
                    day.get(0).get("datum").asText(),
                    MenuJob.DATE_FORMATTER
            );
            for (final JsonNode mealData : day) {
                final String name = mealData.get("nazev").asText();
                if (!canteen.mealValid(mealData))
                    continue; // the canteen has dementia and this meal is just a placeholder

                final String type = mealData.get("druh").asText();
                final Meal.Course course = canteen.course(type);
                final Integer courseNumber = course == Meal.Course.MAIN ? Integer.parseInt(type) : null;
                // get meal
                final Meal meal;
                if (mealDao.existsMealName(name)) {
                    final MealName mealName = mealDao.mealNameByName(name)
                            .orElseThrow(() -> new RuntimeException("Meal name exists, but was not returned!"));
                    meal = mealDao.mealById(mealName.mealId())
                            .orElseThrow(() -> new RuntimeException("Meal id exists, but meal was not returned!"));
                } else {
                    meal = mealDao.insertMeal(new MealSkeleton.Builder()
                            .canteen(canteen)
                            .course(course)
                            .build()
                    );
                    mealDao.insertMealName(new MealNameSkeleton(
                            meal.id(),
                            name
                    ));
                }
                dayEntries.add(new MenuEntrySkeleton.Builder()
                        .date(date)
                        .mealId(meal.id())
                        .courseNumber(courseNumber)
                        .build()
                );
            }
            mealDao.updateMenuDay(date, dayEntries);
        }
    }

    public record MenuRequest(
            @JsonProperty("cislo")
            @NotNull String number
    ) implements Request {
    }
}
