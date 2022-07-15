package nl.gumbomillennium.plazacam;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Webcam {

  private static final int FRAME_BUFFER = 5;
  public final String name;

  private VideoCapture capture;

  public Webcam(String camera) {
    this.name = camera;
  }

  public CompletableFuture<Void> connect() {
    return CompletableFuture.runAsync(
      () -> {
        log.debug("Creating connection handle for {}", name);
        var videoCapture = new VideoCapture(name);

        log.debug("Determining backend for {}", name);
        var backendName = videoCapture.getBackendName();

        log.debug("Opening a connection to {} using {}", name, backendName);
        var openedSuccessfully = videoCapture.open(this.name);

        if (videoCapture.isOpened()) {
          log.info("Successfully connected to {}", name);
          videoCapture.release();
        } else {
          log.warn("Failed to connect to {} using {}", name, backendName);
        }

        if (!openedSuccessfully) {
          throw new RuntimeException("Failed to connect to webcam " + name);
        }

        this.capture = videoCapture;
      });
  }

  public CompletableFuture<Image> getPhoto() {
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

        // Tell the camera to burn some frames, before we retrieve the
        // actual frame. Not all grabs have to be successful, but to prevent
        // an infinite loop, ensure we only try FRAME_BUFFER × 2 at best
        int capturedFrames = 0, attemptsLeft = FRAME_BUFFER * 2;

        while (capturedFrames < FRAME_BUFFER && attemptsLeft > 0) {
          if (this.capture.grab()) {
            capturedFrames++;
          }

          attemptsLeft--;
        }

        // Now perform the actual capture
        var success = this.capture.retrieve(frame);

        // Close the device afterwards
        if (this.capture.isOpened()) {
          this.capture.release();
        }

        // If we failed to grab a frame, throw an exception
        if (!success) {
          throw new RuntimeException("Failed to read frame from webcam " + name);
        }

        // Convert the mat to an image
        var byteMat = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, byteMat);

        try {
          // Convert byte buffer to BufferedImage, for width-height preservation
          var image = ImageIO.read(new ByteArrayInputStream(byteMat.toArray()));

          // Read it back to a ByteBuffer via an output stream
          var outputStream = new ByteArrayOutputStream();
          ImageIO.write(image, "jpg", outputStream);

          // Convert the ByteBuffer to a byte array
          var buffer = ByteBuffer.wrap(outputStream.toByteArray());
          return new Image(name, buffer);
        } catch (IOException exception) {
          log.warn("Failed to render webcam image of {} to BufferedImage", name, exception);
          throw new RuntimeException("Failed to render webcam image of " + name, exception);
        }
      });
  }
}
