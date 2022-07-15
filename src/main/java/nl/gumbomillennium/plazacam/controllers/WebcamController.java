package nl.gumbomillennium.plazacam.controllers;

import lombok.extern.slf4j.Slf4j;
import nl.gumbomillennium.plazacam.models.Image;
import nl.gumbomillennium.plazacam.models.Webcam;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class WebcamController {
  public static final int FRAME_BUFFER = 5;
  private final List<Webcam> webcams;

  public WebcamController() {
    this.webcams = new ArrayList<>();
  }

  public CompletableFuture<Void> registerWebcam(String camera) {
    var webcam = new Webcam(camera);

    var future = connect(webcam);

    future.thenAccept((Void) -> webcams.add(webcam));

    future.thenRun(
      () -> {
        if (future.isCompletedExceptionally()) {
          log.warn("Failed to register webcam {}", camera);
        } else {
          log.debug("Registered webcam: {}", camera);
        }
      });

    return future;
  }

  public CompletableFuture<Image[]> capture() {
    var photoFutures = new ArrayList<CompletableFuture<Image>>();

    for (var webcam : webcams) {
      log.debug("Capturing photo from {}", webcam.name);

      photoFutures.add(capture(webcam));
    }

    var photoFuturesAsArray = photoFutures.toArray(new CompletableFuture[0]);

    log.info("Captured {} photos", photoFuturesAsArray.length);

    return CompletableFuture.allOf(photoFuturesAsArray)
      .thenApply(
        v -> {
          var photos = new ArrayList<Image>();

          log.debug("Captured {} photos", photoFuturesAsArray.length);

          for (var photoFuture : photoFutures) {
            photos.add(photoFuture.isCompletedExceptionally() ? null : photoFuture.join());
          }

          log.debug("Captured {} valid photos", photos.size());

          return photos.toArray(new Image[0]);
        });
  }

  public CompletableFuture<Void> connect(Webcam webcam) {
    return CompletableFuture.runAsync(
      () -> {
        var name = webcam.name;
        var videoCapture = webcam.capture;

        log.debug("Determining backend for {}", name);
        var backendName = videoCapture.getBackendName();

        log.debug("Opening a connection to {} using {}", name, backendName);
        var openedSuccessfully = videoCapture.open(name);

        if (videoCapture.isOpened()) {
          log.info("Successfully connected to {}", name);
          videoCapture.release();
        } else {
          log.warn("Failed to connect to {} using {}", name, backendName);
        }

        if (!openedSuccessfully) {
          throw new RuntimeException("Failed to connect to webcam " + name);
        }
      });
  }

  private CompletableFuture<Image> capture(Webcam camera) {
    return CompletableFuture.supplyAsync(
      () -> {
        var frame = new Mat();
        var capture = camera.capture;
        var name = camera.name;

        if (!capture.isOpened()) {
          log.debug("Opening a connection to {}", name);

          if (!capture.open(name)) {
            log.error("Failed to open camera {}", name);
            throw new RuntimeException("Failed to open webcam " + name);
          }
        }

        // Tell the camera to burn some frames, before we retrieve the
        // actual frame. Not all grabs have to be successful, but to prevent
        // an infinite loop, ensure we only try FRAME_BUFFER × 2 at best
        int capturedFrames = 0, attemptsLeft = FRAME_BUFFER * 2;

        while (capturedFrames < FRAME_BUFFER && attemptsLeft > 0) {
          if (capture.grab()) {
            capturedFrames++;
          }

          attemptsLeft--;
        }

        // Now perform the actual capture
        var success = capture.retrieve(frame);

        // Close the device afterwards
        if (capture.isOpened()) {
          capture.release();
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
