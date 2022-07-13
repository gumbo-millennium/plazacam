package nl.gumbomillennium.plazacam.config;

import com.google.gson.annotations.SerializedName;
import nl.gumbomillennium.plazacam.annotations.IgnoreFromJsonSerialisation;

public class Config {
  public static final String DEFAULT_UPLOAD_URL =
      "https://www.gumbo-millennium.nl/api/plazacam/upload";
  public static final int DEFAULT_INTERVAL = 5;
  @IgnoreFromJsonSerialisation public final boolean isDefault;

  @SerializedName("capture_interval_in_minutes")
  public final int captureIntervalInMinutes;

  @SerializedName("device_name")
  public final String deviceName;

  public final String[] cameras;

  @SerializedName("upload_url")
  public final String uploadUrl;

  @SerializedName("upload_access_token")
  public final String accessToken;

  public Config() {
    this.isDefault = true;
    this.captureIntervalInMinutes = DEFAULT_INTERVAL;
    this.deviceName = "unknown";
    this.cameras = new String[0];
    this.uploadUrl = DEFAULT_UPLOAD_URL;
    this.accessToken = "";
  }

  public Config(
      int captureIntervalInMinutes,
      String deviceName,
      String[] cameras,
      String uploadUrl,
      String accessToken) {
    this.isDefault = false;

    this.captureIntervalInMinutes = captureIntervalInMinutes;
    this.deviceName = deviceName;
    this.cameras = cameras;
    this.uploadUrl = uploadUrl;
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    return new ConfigHandler().configToString(this);
  }
}
