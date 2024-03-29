package cz.jeme.programu.stolujemeapi;

import cz.jeme.programu.stolujemeapi.db.Database;
import cz.jeme.programu.stolujemeapi.db.StoluDatabase;
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
        parseArgs(args);
        SpringApplication.run(Stolujeme.class, args);
        LOGO.forEach(LOGGER::info);
        StoluDatabase.getInstance(); // initialize database
    }

    private static void parseArgs(final String @NotNull [] arguments) {
        if (arguments.length % 2 != 0)
            throw new IllegalArgumentException("Arguments passed must be in a key-value format!");
        for (int arg = 0; arg < arguments.length; arg += 2) {
            String key = arguments[arg];
            if (!key.startsWith("-"))
                throw new IllegalArgumentException("Argument keys must start with a dash!");
            key = key.substring(1);
            String value = arguments[arg + 1];
            args.put(key, value);
        }
        args = Collections.unmodifiableMap(args);
    }

    public static @NotNull List<String> getLogo() {
        return LOGO;
    }

    public static @NotNull Logger getLogger() {
        return LOGGER;
    }

    public static @NotNull Map<String, String> getArgs() {
        return args;
    }

    public static @NotNull Database getDatabase() {
        return StoluDatabase.getInstance();
    }
}