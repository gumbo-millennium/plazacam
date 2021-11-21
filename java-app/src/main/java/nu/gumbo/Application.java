package nu.gumbo;

import com.github.sarxos.webcam.Webcam;
import nu.gumbo.Models.WebcamCollection;
import nu.gumbo.Models.WebcamConfiguration;

import java.time.Clock;
import java.util.*;

/**
 * Hello world!
 */
public final class Application {
    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        // Read the config
        WebcamCollection collection = ConfigurationParser.parse("cams.json");

        // Print available webcams
        System.out.println("Plazacam v3\n\nAvailable webcams:\n");

        for (Webcam webcam : Webcam.getWebcams()) {
            System.out.printf("- %s\n", webcam.getName());
        }

        Application app = new Application(collection);
        app.start();
    }

    private final ArrayList<WebcamCapturer> capturerList;

    private Application(WebcamCollection collection) {
        // Make capturers
        capturerList = new ArrayList<WebcamCapturer>();

        // Init each capturer
        for (WebcamConfiguration config : collection.webcamConfigurations) {
            capturerList.add(new WebcamCapturer(config));
        }
    }

    public void start() {
        try {
            while (true) {
                for (WebcamCapturer capturer: capturerList) {
                    capturer.run();
                }

                waitToNextMinute();
            }
        } catch (InterruptedException exception) {
            System.out.println("Sleep interrrupted, application terminating");
        }
    }

    private void waitToNextMinute() throws InterruptedException {
        Calendar cal = new GregorianCalendar();

        int secondsToMinute = 60 - cal.get(Calendar.SECOND);
        int milliToSecond = 1000 - cal.get(Calendar.MILLISECOND);

        System.out.printf("Waiting for %.3f seconds...\n", secondsToMinute - ((float) milliToSecond) / 1000f);

        Thread.sleep((secondsToMinute * 1000) + milliToSecond);
    }
}
