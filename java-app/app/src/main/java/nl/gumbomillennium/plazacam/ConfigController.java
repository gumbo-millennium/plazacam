package nl.gumbomillennium.plazacam;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public
class ConfigController {
    private final Config config;

    public ConfigController(String path) {
        this.config = this.readOrCreateConfig(path);
    }

    public ConfigController(File file) {
        this.config = this.readOrCreateConfig(file.getAbsolutePath());
    }

    private Config readOrCreateConfig(String path) {
        // Check if file exists
        var file = new File(path);
        var config = Config.buildDefault();

        if (file.exists()) {
            config = Config.buildFromJsonFile(file);
        }

        if (config.isDefault) {
            System.out.println("WARNING: Using a generated default config");

            try {
                Files.writeString(
                    file.toPath(),
                    new Gson().toJson(config)
                );
                System.out.println("Wrote default config to " + file.getAbsolutePath());

            } catch (IOException exception) {
                System.err.println("Failed to write default config file! " + exception.getMessage());
            }
        }

        return config;
    }

    public Config getConfig() {
        return this.config;
    }

    public int getCaptureIntervalInMinutes() {
        return this.config.captureIntervalInMinutes;
    }

    public String getDeviceName() {
        return this.config.deviceName;
    }

    public String[] getCameras() {
        return this.config.cameras;
    }
}
