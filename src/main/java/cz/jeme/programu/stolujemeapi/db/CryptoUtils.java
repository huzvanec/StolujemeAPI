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
    public static final @NotNull String ALGORITHM = "PBKDF2WithHmacSHA512";
    public static final int ITERATIONS = 1 << 16; // 2^16

    public static final int KEY_LENGTH_BITS = 512; // 64 bytes
    public static final int KEY_LENGTH_BASE64 = CryptoUtils.base64Length(CryptoUtils.KEY_LENGTH_BITS / 8);
    public static final int SALT_LENGTH_BYTES = 128; // double the key length
    public static final int SALT_LENGTH_BASE64 = CryptoUtils.base64Length(CryptoUtils.SALT_LENGTH_BYTES);

    public static final int SESSION_LENGTH_BYTES = 64;
    public static final int SESSION_LENGTH_BASE64 = CryptoUtils.base64Length(CryptoUtils.SESSION_LENGTH_BYTES);

    public static final int VERIFICATION_LENGTH_BYTES = 64;
    public static final int VERIFICATION_LENGTH_BASE64 = CryptoUtils.base64Length(CryptoUtils.VERIFICATION_LENGTH_BYTES);

    private static final @NotNull SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final @NotNull SecretKeyFactory KEY_FACTORY;

    static {
        try {
            KEY_FACTORY = SecretKeyFactory.getInstance(CryptoUtils.ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not obtain secret key factory!", e);
        }
    }

    private CryptoUtils() {
        throw new AssertionError();
    }


    public static @NotNull String randomSalt() {
        return CryptoUtils.gen(CryptoUtils.SALT_LENGTH_BYTES);
    }

    public static @NotNull String randomSession() {
        return CryptoUtils.gen(CryptoUtils.SESSION_LENGTH_BYTES);
    }

    public static @NotNull String randomVerification() {
        return CryptoUtils.gen(CryptoUtils.VERIFICATION_LENGTH_BYTES);
    }


    private static @NotNull String gen(final int bytes) {
        final byte[] output = new byte[bytes];
        CryptoUtils.SECURE_RANDOM.nextBytes(output);
        return Base64.getEncoder().encodeToString(output);
    }


    public static @NotNull String hash(final @NotNull String input, final @NotNull String salt) {
        final char[] inputChars = input.toCharArray();
        final byte[] saltBytes = salt.getBytes();

        final PBEKeySpec spec = new PBEKeySpec(inputChars, saltBytes, CryptoUtils.ITERATIONS, CryptoUtils.KEY_LENGTH_BITS);

        Arrays.fill(inputChars, Character.MIN_VALUE);
        final byte[] encoded;
        try {
            encoded = CryptoUtils.KEY_FACTORY.generateSecret(spec).getEncoded();
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Could not generate secret!", e);
        }
        spec.clearPassword();
        return Base64.getEncoder().encodeToString(encoded);
    }

    public static boolean validate(final @NotNull String input, final @NotNull String hash, final @NotNull String salt) {
        return CryptoUtils.hash(input, salt).equals(hash);
    }

    public static int base64Length(final int bytes) {
        // base64 needs 4 chars for every 3 bytes
        final int ceil = (int) Math.ceil(bytes / 3D * 4);
        // the length is always a multiple of 4
        final int remainder = ceil % 4;
        return remainder == 0
                ? ceil
                : ceil + (4 - remainder);
    }
}