package cz.jeme.programu.stolujemeapi;

import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.session.SessionDao;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationDao;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Stolujeme {
    private static final @NotNull List<String> LOGO = List.of(
            "┏┓┏┳┓┏┓┓ ┓┏ ┳┏┓┏┳┓┏┓  ┏┓┏┓┳     ┓ ┏┓",
            "┗┓ ┃ ┃┃┃ ┃┃ ┃┣ ┃┃┃┣   ┣┫┣┛┃  ┓┏ ┃ ┃┃",
            "┗┛ ┻ ┗┛┗┛┗┛┗┛┗┛┛ ┗┗┛  ┛┗┻ ┻  ┗┛ ┻•┗┛"
    );

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(Stolujeme.class);

    private static @NotNull Map<String, String> args = new HashMap<>();


    public static void main(final String @NotNull [] args) {
        Stolujeme.parseArgs(args);
        SpringApplication.run(Stolujeme.class, args);
        Stolujeme.LOGO.forEach(Stolujeme.LOGGER::info);

        // initialize database
        final Object ignored = Database.INSTANCE;
        // initialize daos
        UserDao.INSTANCE.init();
        MealDao.INSTANCE.init();
        VerificationDao.INSTANCE.init();
        SessionDao.INSTANCE.init();
        PhotoDao.INSTANCE.init();
        RatingDao.INSTANCE.init();

        // menu job
        try {
            final SchedulerFactory factory = new StdSchedulerFactory();
            final Scheduler scheduler = factory.getScheduler();
            final JobDetail job = JobBuilder.newJob(MenuJob.class).build();
            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(12, 0))
                    .build();
            scheduler.scheduleJob(job, trigger);
            scheduler.triggerJob(job.getKey());
            scheduler.start();
        } catch (final SchedulerException e) {
            throw new RuntimeException("Could not create menu job!", e);
        }
    }

    private static void parseArgs(final String @NotNull [] arguments) {
        if (arguments.length % 2 != 0)
            throw new IllegalArgumentException("Arguments passed must be in a key-value format!");
        for (int arg = 0; arg < arguments.length; arg += 2) {
            String key = arguments[arg];
            if (!key.startsWith("-"))
                throw new IllegalArgumentException("Argument keys must start with a dash!");
            key = key.substring(1);
            final String value = arguments[arg + 1];
            Stolujeme.args.put(key, value);
        }
        Stolujeme.args = Collections.unmodifiableMap(Stolujeme.args);
    }

    public static @NotNull List<String> logo() {
        return Stolujeme.LOGO;
    }

    public static @NotNull Logger logger() {
        return Stolujeme.LOGGER;
    }

    public static @NotNull Map<String, String> args() {
        return Stolujeme.args;
    }

    @Bean
    protected @NotNull WebMvcConfigurer corsConfig() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(final @NotNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000");
            }
        };
    }
}