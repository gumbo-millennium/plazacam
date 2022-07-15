package nl.gumbomillennium.plazacam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import nl.gumbomillennium.plazacam.controllers.WebcamController;
import nl.gumbomillennium.plazacam.models.Config;
import nl.gumbomillennium.plazacam.models.Webcam;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ConfigHandler {
  private final Gson parser;

  public ConfigHandler() {
    this.parser =
      new GsonBuilder()
        .setExclusionStrategies(new AnnotationExclusionStrategy())
        .setPrettyPrinting()
        .serializeNulls()
        .create();
  }

  public String configToString(Config config) {
    return this.parser.toJson(config);
  }

  public void writeConfig(Config config, File file) throws ConfigurationException {
    var jsonContents = configToString(config);
    try {
      Files.writeString(file.toPath(), jsonContents);
    } catch (IOException exception) {
      throw new ConfigurationException("Failed to write config file! " + exception.getMessage());
    }
  }

  public Config readConfig(File file) throws ConfigurationException {
    // Fail if missing
    if (!file.exists()) {
      throw new ConfigurationException("Config file does not exist!");
    }

    // Read file
    var json = "";
    try {
      json = Files.readString(file.toPath());
    } catch (IOException exception) {
      throw new ConfigurationException("Failed to read config file! " + exception.getMessage());
    }

    // Parse to config
    try {
      return this.parser.fromJson(json, Config.class);
    } catch (JsonSyntaxException exception) {
      throw new ConfigurationException("Failed to parse config file! " + exception.getMessage());
    }
  }

  public Config buildDefaultConfig() {
    return new Config(
      Config.DEFAULT_INTERVAL,
      UUID.randomUUID().toString(),
      determineDefaultWebcams(),
      Config.DEFAULT_UPLOAD_URL,
      "");
  }

  /**
   * Determines all valid webcams on the system, based on an arbitrary guess
   */
  public String[] determineDefaultWebcams() {
    var possibleCams = determinePossibleWebcams();

    if (possibleCams.length == 0) {
      return new String[0];
    }

    return determineValidWebcams(possibleCams);
  }

  /**
   * Creates a list of possible webcams, which is unchecked except for IO existence.
   */
  protected String[] determinePossibleWebcams() {
    var foundDevices = new ArrayList<String>();

    // Check video 0 - 10
    for (int i = 0; i < 10; i++) {
      var device = String.format("/dev/video%d", i);
      if (new File(device).exists()) {
        foundDevices.add(device);
      }
    }

    // Report
    log.debug("Found {} possible webcams", foundDevices.size());

    return foundDevices.toArray(new String[0]);
  }

  /**
   * Validates if the given list of webcams is valid, by actually making a connection.
   */
  protected String[] determineValidWebcams(String[] webcams) {
    var validWebcams = new ArrayList<String>();
    var webcamController = new WebcamController();

    // Determine if all devices are valid devices, synchronously
    for (String device : webcams) {
      var webcam = new Webcam(device);
      try {
        // Try to connect cameras, wait for 5 seconds at most
        webcamController.connect(webcam).get(5L, TimeUnit.SECONDS);

        // Webcam valid
        validWebcams.add(device);
      } catch (InterruptedException | CancellationException exception) {
        // Seems like we're disturbed
        log.info("Webcam lookup failed at {}, seems to be an interruption", device, exception);
        System.err.println("Webcam lookup was cancelled");
        return new String[0];
      } catch (ExecutionException | TimeoutException exception) {
        log.debug("Dropping webcam {}, likely invalid.", device);
        // Webcam probably invalid
      }
    }

    // Report
    log.info("Found {} valid webcams", validWebcams.size());

    // Return as array
    return validWebcams.toArray(new String[0]);
  }
}
