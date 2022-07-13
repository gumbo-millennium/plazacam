package nl.gumbomillennium.plazacam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UploadController {
  private final String uploadUrl;
  private final String deviceName;
  private final String accessToken;
  private final Path tempDirectory;

  public UploadController(
      String tempDirectory, String deviceName, String uploadUrl, String accessToken) {
    this.deviceName = deviceName;
    this.uploadUrl = uploadUrl;
    this.accessToken = accessToken;
    log.info("Uploading as {} to {}", deviceName, uploadUrl);

    this.tempDirectory = Paths.get(tempDirectory, "temp");
    if (!this.tempDirectory.toFile().exists()) {
      this.tempDirectory.toFile().mkdirs();
      log.debug("Created temp directory: {}", this.tempDirectory);
    }
  }

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

  private void doUpload(Image photo) {

    // Sleep for some random interval to simulate a real upload
    var random = (int) (Math.random() * 10 * 1000);
    try {
      Thread.sleep(random);
    } catch (InterruptedException exception) {
      // Eh
    }
  }
}
