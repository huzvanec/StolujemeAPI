package cz.jeme.programu.stolujemeapi;

import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.session.SessionDao;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.db.verification.VerificationDao;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        UserDao.INSTANCE.init();
        VerificationDao.INSTANCE.init();
        SessionDao.INSTANCE.init();
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
}