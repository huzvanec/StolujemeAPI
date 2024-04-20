package cz.jeme.programu.stolujemeapi.rest.control;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.Stolujeme;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.photo.Photo;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoSkeleton;
import cz.jeme.programu.stolujemeapi.db.session.Session;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Request;
import cz.jeme.programu.stolujemeapi.rest.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@RestController
public final class PhotoController {
    public static final int IMAGE_DIMENSION_MAX = 2000; // px
    public static final @Range(from = 0, to = 100) int IMAGE_QUALITY = 50; // %
    public static final @NotNull Duration MAX_PHOTO_AGE = Duration.ofDays(7);

    private PhotoController() {
    }

    private final @NotNull InvalidParamException invalidPhoto = new InvalidParamException("photo", ApiErrorType.PHOTO_CONTENTS_INVALID);
    private final @NotNull File photoDir = new File(Objects.requireNonNull(Stolujeme.args().get("photos"), "photos"));

    {
        if (!photoDir.mkdirs() && !photoDir.isDirectory())
            throw new IllegalArgumentException("Image directory is not a directory!");
    }

    @PostMapping("/photo")
    @ResponseBody
    private @NotNull Response photo(final @NotNull @RequestBody PhotoPostRequest request) {
        final Session session = ApiUtils.authenticate();
        final byte[] imageData;
        try {
            imageData = Base64.getDecoder().decode(ApiUtils.require(request.imageData(), "photo"));
        } catch (final IllegalArgumentException e) {
            throw invalidPhoto;
        }

        final UUID mealUuid = ApiUtils.parseUuid(request.mealUuid(), "mealUuid");
        final Meal meal = MealDao.INSTANCE.mealByUuid(mealUuid)
                .orElseThrow(() -> new InvalidParamException("mealUuid", ApiErrorType.MEAL_UUID_INVALID));

        final Duration photoAge = Duration.between(getTimeTaken(imageData), LocalDateTime.now());
        if (photoAge.compareTo(PhotoController.MAX_PHOTO_AGE) > 0)
            // photo age is greater than maximum photo age
            throw invalidPhoto;

        final UUID photoUuid = UUID.randomUUID();

        final File mealDir = new File(Path.of(photoDir.getAbsolutePath(), meal.uuid().toString()).toString());
        mealDir.mkdir();
        final String outputPath = Path.of(mealDir.getAbsolutePath(), photoUuid + ".avif").toString();

        final ProcessBuilder processBuilder = new ProcessBuilder(
                "convert", "-",
                "-resize", PhotoController.IMAGE_DIMENSION_MAX + 'x' + PhotoController.IMAGE_DIMENSION_MAX + ">",
                "-quality", String.valueOf(PhotoController.IMAGE_QUALITY),
                outputPath
        );
        final Process process;
        try {
            process = processBuilder.start();
            process.getOutputStream().write(imageData);
            process.getOutputStream().close();
            final int exitCode = process.waitFor();
            if (exitCode != 0)
                throw new RuntimeException("Imagemagick process exited with code " + exitCode);
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("An error occurred while executing imagemagick!", e);
        }

        final Photo photo = PhotoDao.INSTANCE.insertPhoto(new PhotoSkeleton.Builder()
                .userId(session.userId())
                .mealId(meal.id())
                .uuid(photoUuid)
                .file(new File(outputPath))
                .build());

        return new PhotoPostResponse(
                photoUuid,
                photo.uploadedTime()
        );
    }

    private @NotNull LocalDateTime getTimeTaken(final byte @NotNull [] imageData) {
        final Metadata meta;
        try {
            meta = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageData));
        } catch (final ImageProcessingException | IOException e) {
            throw invalidPhoto;
        }

        if (meta.containsDirectoryOfType(HeifDirectory.class)) {
            // HEIC
            final Directory heif = Objects.requireNonNull(meta.getFirstDirectoryOfType(HeifDirectory.class));
            final String majorBrand = photoRequire(heif.getString(HeifDirectory.TAG_MAJOR_BRAND));
            if (!majorBrand.equals("heic")) throw invalidPhoto;
        } else if (meta.containsDirectoryOfType(JpegDirectory.class)) {
            // JPEG
        } else {
            throw invalidPhoto;
        }
        final ExifSubIFDDirectory exif = photoRequire(meta.getFirstDirectoryOfType(ExifSubIFDDirectory.class));
        final Date date = photoRequire(exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private <T> @NotNull T photoRequire(final @Nullable T param) {
        if (param == null) throw invalidPhoto;
        return param;
    }


    public record PhotoPostRequest(
            @JsonProperty("mealUuid")
            @Nullable String mealUuid,
            @JsonProperty("photo")
            @Nullable String imageData
    ) implements Request {
    }

    public record PhotoPostResponse(
            @JsonProperty("photoUuid")
            @NotNull UUID photoUuid,
            @JsonProperty("uploadedTime")
            @NotNull LocalDateTime uploadedTime
    ) implements Response {
    }

    @GetMapping("/photo")
    @ResponseBody
    private @NotNull ResponseEntity<InputStreamResource> photo(final @NotNull @RequestBody PhotoGetRequest request) {
        ApiUtils.authenticate();
        final UUID photoUuid = ApiUtils.parseUuid(request.photoUuid(), "photoUuid");


        final Photo photo = PhotoDao.INSTANCE.photoByUuid(photoUuid)
                .orElseThrow(() -> new InvalidParamException("photoUuid", ApiErrorType.PHOTO_UUID_INVALID));


        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/avif"))
                    .body(new InputStreamResource(new FileInputStream(photo.file())));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("The file does not exist!", e);
        }
    }

    public record PhotoGetRequest(
            @JsonProperty("photoUuid")
            @Nullable String photoUuid
    ) implements Request {
    }
}
