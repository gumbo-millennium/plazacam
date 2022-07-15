package nl.gumbomillennium.plazacam;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UploadController {
  private final String uploadUrl;
  private final String deviceName;
  private final String accessToken;
  private final Path tempDirectory;
  private final OkHttpClient httpClient;

  /**
   * Create a new upload controller, most variables should be from the configuration.
   *
   * @param tempDirectory
   * @param deviceName
   * @param uploadUrl
   * @param accessToken
   */
  public UploadController(
    String tempDirectory, String deviceName, String uploadUrl, String accessToken) {
    // Set the props
    this.deviceName = deviceName;
    this.uploadUrl = uploadUrl;
    this.accessToken = accessToken;
    log.info("Uploading as {} to {}", deviceName, uploadUrl);

    // Create the client
    this.httpClient = OkHttpClient.builder().build();

    // Create temp directory, if not found
    this.tempDirectory = Paths.get(tempDirectory, "temp");
    if (!this.tempDirectory.toFile().exists()) {
      this.tempDirectory.toFile().mkdirs();
      log.debug("Created temp directory: {}", this.tempDirectory);
    }
  }

  /**
   * Uploads the given photo to the server.
   * The upload won't be accessible, unless the device-name-mapping is
   * registered as belonging to a cam.
   *
   * @param photo The photo to upload.
   * @return A future that will be completed when the upload is complete.
   */
  public CompletableFuture<Void> upload(Image photo) {
    return CompletableFuture.runAsync(
      () -> {
        // Start tracking the upload
        var startTime = System.currentTimeMillis();
        log.info("Uploading photo {} to {}", photo.name, uploadUrl);

        // Store in cache folder
        doTempStore(photo);

        // Perform the actual upload elsewhere
        doUpload(photo);

        // Determine duration and report
        var duration = System.currentTimeMillis() - startTime;
        log.info("Photo {} uploaded in {} seconds", photo.name, duration / 1000.0);
      });
  }

  /**
   * Upload all photos in the collection, returning when all uploads
   * have completed (failed or completed, either way)
   *
   * @param photos List of photos
   * @return A future that will be completed when all uploads are finished.
   */
  public CompletableFuture<Void> upload(Image[] photos) {
    // Keep track of futures
    var futures = new ArrayList<CompletableFuture<Void>>();

    // Keep track of time
    var startTime = System.currentTimeMillis();

    // Upload photos
    for (var photo : photos) {
      if (photo != null) {
        futures.add(upload(photo));
      }
    }

    // Wait for all uploads to complete, and report duration
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
      .thenRun(
        () -> {
          var duration = System.currentTimeMillis() - startTime;
          log.info("Uploaded {} photos in {} seconds", futures.size(), duration / 1000.0);
        });
  }

  /**
   * As a debug reason, write the photos to disk for inspection.
   * Overwrite existing photos, it's not a backup.
   *
   * @param photo
   */
  private void doTempStore(Image photo) {
    // Store file on disk
    var sluggedImageName =
      photo.name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    var filePath = Paths.get(tempDirectory.toString(), sluggedImageName);

    try {
      Files.write(filePath, photo.photo.array());
    } catch (IOException exception) {
      log.error("Failed to write photo to disk: {}", filePath, exception);
    }
  }

  /**
   * Send the photo to the server. Note that it will not be accessible
   * unless the device-name-mapping is registered as belonging to a cam
   * on the server (not neccesarily accessible for us).
   *
   * @param photo
   */
  private void doUpload(Image photo) {
    // Build the body
    RequestBody requestBody = new MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("device", this.deviceName)
      .addFormDataPart("name", photo.name)
      .addFormDataPart("photo", "plazacam.jpg",
        RequestBody.create(MEDIA_TYPE_JPG, photo.photo))
      .build();

    // Build the request
    Request request = new Request.Builder()
      .header("Authorization", "Bearer " + this.accessToken)
      .header("User-Agent", USER_AGENT)
      .header("X-Plazacam-Version", )
      .url(this.uploadUrl)
      .post(requestBody)
      .build();

    // Call the server using the multipart body, in a try-with statement
    // so it gets quickly gc'd
    try (Response response = httpClient.newCall(request).execute()) {
      // Expect HTTP 200
      if (!response.isSuccessful()) {
        log.warn("Failed to upload photo: {}", response.message());
        throw new RuntimeException("Unexpected code " + response);
        return;
      }

      // Require an Accepted status code
      if (response.code() != = Http.ACCEPTED) {
        log.warn("Failed to upload photo: {}", response.message());
        throw new RuntimeException("Unexpected code " + response);
        return;
      }

      // DOne
      log.debug("Upload call of {image} completed", photo.name);
    }
  }

  /**
   * Get the version of the application
   *
   * @return
   */
  public String getApplicationVersion() {
    return getClass().getPackage().getImplementationVersion();
  }
}
