package nu.gumbo.Models;

import org.json.JSONObject;

public class WebcamConfiguration {

    public static WebcamConfiguration fromJson(JSONObject object) {
        return new WebcamConfiguration(
            object.getString("device"),
            object.optString("name", "Webcam"),
            object.getString("url"),
            object.has("sleep") ? object.getFloat("sleep") : 0f
        );
    }

    public final String source;
    public final String name;
    public final String submitUrl;
    public final float sleepInterval;


    public WebcamConfiguration(String source, String name, String submitUrl, float sleepInterval)
    {
        this.source = source;
        this.name = name;
        this.submitUrl = submitUrl;
        this.sleepInterval = sleepInterval;
    }
}
