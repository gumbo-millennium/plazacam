package nl.gumbomillennium.plazacam;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import nl.gumbomillennium.plazacam.annotations.IgnoreFromJsonSerialisation;

public class Config {
  @IgnoreFromJsonSerialisation public final boolean isDefault;

  @SerializedName("capture_interval_in_minutes")
  public final int captureIntervalInMinutes;

  @SerializedName("device_name")
  public final String deviceName;

  public final String[] cameras;

  public Config(int captureIntervalInMinutes, String deviceName, String[] cameras) {
    this(captureIntervalInMinutes, deviceName, cameras, false);
  }

  public Config(
      int captureIntervalInMinutes, String deviceName, String[] cameras, boolean isDefault) {
    this.captureIntervalInMinutes = captureIntervalInMinutes;
    this.deviceName = deviceName;
    this.cameras = cameras;
    this.isDefault = isDefault;
  }

  /**
   * Returns a default config, with a random device name
   *
   * @return Default config, random device name
   */
  static Config buildDefault() {
    // Build a default config with a random UUID as device name
    return new Config(5, UUID.randomUUID().toString(), listCaptureDevices(), true);
  }

  /**
   * Reads the config from the file, or returns a default config
   *
   * @param file File instance
   * @return Read config or default config
   */
  static Config buildFromJsonFile(File file) {
    // Check if the file exists
    if (!file.exists()) {
      return buildDefault();
    }

    try {
      // Read the file
      var jsonString = Files.readString(file.toPath());

      // Convert to JSON
      return new Gson().fromJson(jsonString, Config.class);
    } catch (Exception e) {
      System.err.println("Error reading config file: " + e.getMessage());
      return buildDefault();
    }
  }

  private static String[] listCaptureDevices() {
    var foundDevices = new ArrayList<String>();

    // Check video 0 - 10
    for (int i = 0; i < 10; i++) {
      var device = String.format("/dev/video%d", i);
      if (new File(device).exists()) {
        foundDevices.add(device);
      }
    }

    // Return as array
    return foundDevices.toArray(new String[0]);
  }
}
