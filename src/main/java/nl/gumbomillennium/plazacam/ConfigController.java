package nl.gumbomillennium.plazacam;

import lombok.extern.slf4j.Slf4j;
import nl.gumbomillennium.plazacam.config.Config;
import nl.gumbomillennium.plazacam.config.ConfigHandler;

import javax.naming.ConfigurationException;
import java.io.File;

@Slf4j
public class ConfigController {
  private final Config config;

  public ConfigController(File file) {
    this.config = this.readOrCreateConfig(file);
  }

  private Config readOrCreateConfig(File file) {
    var handler = new ConfigHandler();

    // Create default config if no file exists
    if (!file.exists()) {
      var defaultConfig = handler.buildDefaultConfig();

      try {
        handler.writeConfig(defaultConfig, file);
        log.info("Wrote default config file to {}", file.getAbsolutePath());
      } catch (ConfigurationException exception) {
        log.warn("Failed to write default config", exception);
        throw new RuntimeException("Failed to write default config", exception);
      }
    }

    // Read the config
    try {
      return handler.readConfig(file);
    } catch (ConfigurationException exception) {
      log.warn("Failed to read config", exception);
      throw new RuntimeException("Failed to read config", exception);
    }
  }

  public Config getConfig() {
    return this.config;
  }
}
