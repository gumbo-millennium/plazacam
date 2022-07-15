package nl.gumbomillennium.plazacam;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {
  static {
    try {
      nu.pattern.OpenCV.loadLocally();
    } catch (RuntimeException exception) {
      log.error("Failed to load OpenCV!", exception);
      throw new RuntimeException("Failed to load OpenCV", exception);
    }
  }

  public static void main(String[] args) {
    // Check OS, only Linux is supported
    var os = System.getProperty("os.name");
    if (!os.toLowerCase().contains("linux")) {
      log.error("OS '{}' is not supported", os);
      System.exit(1);
    }

    // Create required directory
    var appDirectory = getApplicationDirectory();

    var app = new App();

    try {
      app.loadConfig(appDirectory);
      log.info("Load configuration");
    } catch (Exception exception) {
      log.error("Failed to load configuration", exception);
      System.exit(1);
    }

    try {
      app.registerWebcams().get();
      log.info("Registered webcams");
    } catch (Exception exception) {
      log.error("Failed to register webcams", exception);
      System.exit(1);
    }

    try {
      app.registerUploader(appDirectory);
      log.info("Registered uploader");
    } catch (Exception exception) {
      log.error("Failed to register uploader", exception);
      System.exit(1);
    }

    log.info("Starting application");

    // Start a scheduler with the app as scheduled task
    var captureExecutor = Executors.newSingleThreadScheduledExecutor();
    var waitingFuture =
      captureExecutor.scheduleAtFixedRate(
        app, 0, app.getConfig().captureIntervalInMinutes, TimeUnit.MINUTES);

    try {
      waitingFuture.get();
    } catch (Exception exception) {
      log.error("Failed to start application", exception);
      System.exit(1);
    }
  }

  private static String getApplicationDirectory() {
    var rootDirectory = System.getProperty("user.home");
    var appDirectory = Paths.get(rootDirectory, ".plazacam");

    if (!appDirectory.toFile().exists()) {
      appDirectory.toFile().mkdir();
    }

    return appDirectory.toString();
  }
}
