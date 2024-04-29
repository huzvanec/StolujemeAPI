package cz.jeme.programu.stolujemeapi;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public enum Lang {
    CZ(
            "czech",
            "[Stolujeme] Ověřte svou e-mailovou adresu"
    ),
    EN(
            "english",
            "[Stolujeme] Verify your email address"
    );

    private final @NotNull String verification = readResource("verification");
    private final @NotNull String name;
    private final @NotNull String verificationSubject;

    Lang(final @NotNull String name, final @NotNull String verificationSubject) {
        this.name = name;
        this.verificationSubject = verificationSubject;
    }

    @Override
    public @NotNull String toString() {
        return name;
    }

    private @NotNull String readResource(final @NotNull String resource) {
        final String suffix = '.' + name().toLowerCase();
        try (final InputStream stream = getClass().getResourceAsStream("/lang/" + resource + suffix)) {
            if (stream == null) throw new IllegalArgumentException("This resource does not exist!");
            final Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (final IOException e) {
            throw new RuntimeException("Could not read resource stream!", e);
        }
    }

    public @NotNull String verification() {
        return verification;
    }

    public @NotNull String verificationSubject() {
        return verificationSubject;
    }
}
