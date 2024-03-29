package cz.jeme.programu.stolujemeapi.db;

import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public final class CryptoUtils {
    private static final @NotNull SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final @NotNull String ALGORITHM = "PBKDF2WithHmacSHA512";
    public static final int ITERATIONS = 1 << 16; // 2^16 = 65536
    public static final int KEY_LENGTH_BITS = 512;
    public static final int KEY_LENGTH_BASE64 = base64Length(KEY_LENGTH_BITS / 8);
    public static final int SALT_LENGTH_BYTES = 128;
    public static final int SALT_LENGTH_BASE64 = base64Length(SALT_LENGTH_BYTES);
    public static final int TOKEN_LENGTH_BYTES = 64;
    public static final int TOKEN_LENGTH_BASE64 = base64Length(TOKEN_LENGTH_BYTES);
    public static final int VERIFICATION_LENGTH_BYTES = 64;
    public static final int VERIFICATION_LENGTH_BASE64 = base64Length(VERIFICATION_LENGTH_BYTES);

    private static final @NotNull SecretKeyFactory KEY_FACTORY;

    static {
        try {
            KEY_FACTORY = SecretKeyFactory.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not obtain secret key factory!", e);
        }
    }

    private CryptoUtils() {
        throw new AssertionError();
    }


    public static @NotNull String genSalt() {
        return gen(SALT_LENGTH_BYTES);
    }

    public static @NotNull String genToken() {
        return gen(TOKEN_LENGTH_BYTES);
    }

    public static @NotNull String genVerification() {
        return gen(VERIFICATION_LENGTH_BYTES);
    }


    private static @NotNull String gen(final int bytes) {
        byte[] output = new byte[bytes];
        SECURE_RANDOM.nextBytes(output);
        return Base64.getEncoder().encodeToString(output);
    }


    public static @NotNull String hash(final @NotNull String input, final @NotNull String salt) throws InvalidKeySpecException {
        char[] inputChars = input.toCharArray();
        byte[] saltBytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(inputChars, saltBytes, ITERATIONS, KEY_LENGTH_BITS);

        Arrays.fill(inputChars, Character.MIN_VALUE);

        byte[] inputEncoded = KEY_FACTORY.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return Base64.getEncoder().encodeToString(inputEncoded);
    }

    public static boolean validate(final @NotNull String input, final @NotNull String hash, final @NotNull String salt) throws InvalidKeySpecException {
        return hash(input, salt).equals(hash);
    }

    public static int base64Length(final int bytes) {
        // base64 needs 4 chars for every 3 bytes
        int ceil = (int) Math.ceil(bytes / 3D * 4);
        // the length is always a multiple of 4
        int remainder = ceil % 4;
        return remainder == 0
                ? ceil
                : ceil + (4 - remainder);
    }
}
