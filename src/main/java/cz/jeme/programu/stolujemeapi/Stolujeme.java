package cz.jeme.programu.stolujemeapi;

import cz.jeme.programu.stolujemeapi.canteen.Canteen;
import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
import cz.jeme.programu.stolujemeapi.db.rating.RatingDao;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
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

import java.util.List;

@SpringBootApplication
public class Stolujeme {
    private static final @NotNull List<String> LOGO = List.of(
            "┏┓┏┳┓┏┓┓ ┓┏ ┳┏┓┏┳┓┏┓  ┏┓┏┓┳     ┓ ┏┓",
            "┗┓ ┃ ┃┃┃ ┃┃ ┃┣ ┃┃┃┣   ┣┫┣┛┃  ┓┏ ┃ ┃┃",
            "┗┛ ┻ ┗┛┗┛┗┛┗┛┗┛┛ ┗┗┛  ┛┗┻ ┻  ┗┛ ┻•┗┛"
    );

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(Stolujeme.class);

    public static void main(final String @NotNull [] args) {
        EnvVar.class.getEnumConstants(); // load environmental variables
        SpringApplication.run(Stolujeme.class, args); // start spring boot
        Stolujeme.LOGO.forEach(Stolujeme.LOGGER::info); // STOLUJEME 😎

        // initialize database
        Database.INSTANCE.init();
        // initialize data access objects
        UserDao.INSTANCE.init();
        MealDao.INSTANCE.init();
        PhotoDao.INSTANCE.init();
        RatingDao.INSTANCE.init();

        // initialize canteens
        Canteen.init();

        // menu job
        try {
            final SchedulerFactory factory = new StdSchedulerFactory();
            final Scheduler scheduler = factory.getScheduler();
            final JobDetail job = JobBuilder.newJob(MenuJob.class).build();
            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(12, 0))
                    .build();
            scheduler.scheduleJob(job, trigger);
            scheduler.triggerJob(job.getKey()); // run the job at the start
            scheduler.start();
        } catch (final SchedulerException e) {
            throw new RuntimeException("Could not create menu job!", e);
        }
    }

    public static @NotNull List<String> logo() {
        return Stolujeme.LOGO;
    }

    public static @NotNull Logger logger() {
        return Stolujeme.LOGGER;
    }

    @Bean
    protected @NotNull WebMvcConfigurer cors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(final @NotNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173",
                                "https://stolu.jeme.cz"
                        ); // TODO
            }
        };
    }
}