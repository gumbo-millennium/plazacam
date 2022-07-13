package nl.gumbomillennium.plazacam;

import java.util.concurrent.CompletableFuture;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Webcam {

  private final String name;

  private VideoCapture capture;

  public Webcam(String camera) {
    this.name = camera;
  }

  public CompletableFuture<Void> connect() {
    return CompletableFuture.runAsync(
      () -> {
        System.out.println("Creating handle for webcam " + name);
        var videoCapture = new VideoCapture(this.name);

        System.out.println("Capturing backend name for webcam " + name);
        var deviceName = videoCapture.getBackendName();

        System.out.println("Opening handle for camera " + deviceName + " (" + name + ")");

        videoCapture.open(this.name);

        if (videoCapture.isOpened()) {
          System.out.println("Opened handle for camera " + deviceName + " (" + name + ")");
          videoCapture.release();
        } else {
          System.out.println(
              "Failed to open handle for camera " + deviceName + " (" + name + ")");
        }

        this.capture = videoCapture;
      });
  }

  public CompletableFuture<CharSequence> getPhoto() {
    return CompletableFuture.supplyAsync(
        () -> {
          var frame = new Mat();

          if (!this.capture.isOpened()) {
            System.out.println("Webcam " + name + " is not opened, opening");
            if (!this.capture.open(this.name)) {
              System.out.println("Failed to open webcam " + name);
              throw new RuntimeException("Failed to open webcam " + name);
            }
          }

          var success = this.capture.read(frame);

          if (this.capture.isOpened()) {
            this.capture.release();
          }

          if (!success) {
            throw new RuntimeException("Failed to read frame from webcam " + name);
          }

          return frame.toString();
        });
  }
}
