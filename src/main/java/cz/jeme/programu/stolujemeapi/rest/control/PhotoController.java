package cz.jeme.programu.stolujemeapi.rest.control;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.jeme.programu.stolujemeapi.EnvVar;
import cz.jeme.programu.stolujemeapi.db.meal.Meal;
import cz.jeme.programu.stolujemeapi.db.meal.MealDao;
import cz.jeme.programu.stolujemeapi.db.photo.Photo;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoDao;
import cz.jeme.programu.stolujemeapi.db.photo.PhotoSkeleton;
import cz.jeme.programu.stolujemeapi.db.user.Session;
import cz.jeme.programu.stolujemeapi.db.user.User;
import cz.jeme.programu.stolujemeapi.db.user.UserDao;
import cz.jeme.programu.stolujemeapi.error.ApiErrorType;
import cz.jeme.programu.stolujemeapi.error.InvalidParamException;
import cz.jeme.programu.stolujemeapi.rest.ApiUtils;
import cz.jeme.programu.stolujemeapi.rest.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
public final class PhotoController {
    public static final int IMAGE_DIMENSION_MAX = 2000; // px
    public static final @Range(from = 0, to = 100) int IMAGE_QUALITY = 40; // %
    public static final @NotNull Duration MAX_PHOTO_AGE = Duration.ofDays(7);
    public static final @NotNull MediaType IMAGE_AVIF = MediaType.valueOf("image/avif");

    private PhotoController() {
    }

    private final @NotNull InvalidParamException invalidPhoto = new InvalidParamException("photo", ApiErrorType.PHOTO_CONTENTS_INVALID);
    private final @NotNull File photoDir = new File(EnvVar.PHOTO_DIR.require());

    {
        if (!photoDir.mkdirs() && !photoDir.isDirectory())
            throw new IllegalArgumentException("Image directory is not a directory!");
    }

    @PostMapping(value = "/meals/{mealUuid}/photos", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    private @NotNull Response uploadPhoto(final @NotNull @PathVariable("mealUuid") String mealUuidStr,
                                          final @NotNull @RequestParam("photo") MultipartFile file,
                                          final @NotNull HttpServletResponse response) {
        final Session session = ApiUtils.authenticate();
        final byte[] imageData;
        try {
            imageData = file.getBytes();
        } catch (final IOException e) {
            throw invalidPhoto;
        }

        final UUID mealUuid = ApiUtils.parseUuid(mealUuidStr, "mealUuid");
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
                "convert",
                "-",
                "-resize",
                PhotoController.IMAGE_DIMENSION_MAX + "x" + PhotoController.IMAGE_DIMENSION_MAX + ">",
                "-quality",
                String.valueOf(PhotoController.IMAGE_QUALITY),
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

        response.addHeader("Location", "/meals/" + mealUuid + "/photos/" + photoUuid);
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

    public record PhotoPostResponse(
            @JsonProperty("photoUuid")
            @NotNull UUID photoUuid,
            @JsonProperty("uploadedTime")
            @NotNull LocalDateTime uploadedTime
    ) implements Response {
    }

    private @NotNull List<Photo> getPhotosByMealUuid(final @NotNull String mealUuidStr) {
        final UUID mealUuid = ApiUtils.parseUuid(mealUuidStr, "mealUuid");
        final Meal meal = MealDao.INSTANCE.mealByUuid(mealUuid)
                .orElseThrow(() -> new InvalidParamException("mealUuid", ApiErrorType.MEAL_UUID_INVALID));
        return PhotoDao.INSTANCE.photosByMealId(meal.id());
    }

    private @NotNull Photo getPhotoByUuidMealUuid(final @NotNull String mealUuidStr, final @NotNull String photoUuidStr) {
        final UUID photoUuid = ApiUtils.parseUuid(photoUuidStr, "photoUuid");
        return getPhotosByMealUuid(mealUuidStr).stream()
                .filter(p -> p.uuid().equals(photoUuid))
                .findFirst()
                .orElseThrow(() -> new InvalidParamException("photoUuid", ApiErrorType.PHOTO_UUID_INVALID));
    }

    @GetMapping("/meals/{mealUuid}/photos")
    @ResponseBody
    private @NotNull Response getPhotos(final @NotNull @PathVariable("mealUuid") String mealUuidStr) {
        ApiUtils.authenticate();
        return new PhotosResponse(getPhotosByMealUuid(mealUuidStr).stream().map(Photo::uuid).toList());
    }

    public record PhotosResponse(
            @JsonProperty("photos")
            @NotNull List<UUID> photos
    ) implements Response {
    }

    @GetMapping("/meals/{mealUuid}/photos/{photoUuid}")
    @ResponseBody
    private @NotNull Response getPhotoInfo(final @NotNull @PathVariable("mealUuid") String mealUuidStr,
                                           final @NotNull @PathVariable("photoUuid") String photoUuidStr) {
        ApiUtils.authenticate();

        final Photo photo = getPhotoByUuidMealUuid(mealUuidStr, photoUuidStr);
        final User user = UserDao.INSTANCE.userById(photo.userId())
                .orElseThrow(() -> new RuntimeException("Could not find user from photo!"));
        return new PhotoResponse(new PhotoData(
                photo.uuid(),
                user.name(),
                photo.fileSize(),
                photo.uploadedTime()
        ));
    }

    public record PhotoResponse(
            @JsonProperty("photo")
            @NotNull PhotoData photoData
    ) implements Response {
    }

    public record PhotoData(
            @JsonProperty("uuid")
            @NotNull UUID uuid,
            @JsonProperty("author")
            @NotNull String author,
            @JsonProperty("fileSize")
            long fileSize,
            @JsonProperty("uploadedTime")
            @NotNull LocalDateTime uploadedTime
    ) {
    }


    @GetMapping("/meals/{mealUuid}/photos/{photoUuid}/view")
    @ResponseBody
    private @NotNull ResponseEntity<InputStreamResource> viewPhoto(final @NotNull @PathVariable("mealUuid") String mealUuidStr,
                                                                   final @NotNull @PathVariable("photoUuid") String photoUuidStr) {
        ApiUtils.authenticate();

        final Photo photo = getPhotoByUuidMealUuid(mealUuidStr, photoUuidStr);

        try {
            return ResponseEntity.ok()
                    .contentType(PhotoController.IMAGE_AVIF)
                    .body(new InputStreamResource(new FileInputStream(photo.file())));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("The file does not exist!", e);
        }
    }
}
