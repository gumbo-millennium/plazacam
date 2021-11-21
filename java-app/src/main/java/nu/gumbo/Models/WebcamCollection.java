package nu.gumbo.Models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WebcamCollection {
    public static WebcamCollection fromJson(JSONArray cameras) {
        WebcamCollection config = new WebcamCollection();

        // Assign all cameras
        for (int iterator = 0; iterator < cameras.length(); iterator++) {
            JSONObject camera = cameras.optJSONObject(iterator);

            if (camera == null)
                continue;

            config.addWebcam(WebcamConfiguration.fromJson(camera));
        }

        // Config is done
        return config;
    }

    public final ArrayList<WebcamConfiguration> webcamConfigurations;

    public WebcamCollection() {
        webcamConfigurations = new ArrayList<>();
    }

    public void addWebcam(WebcamConfiguration webcamConfiguration) {
        this.webcamConfigurations.add(webcamConfiguration);
    }
}
