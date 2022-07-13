package nl.gumbomillennium.plazacam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebcamController {
  private final List<Webcam> webcams;

  public WebcamController() {
    this.webcams = new ArrayList<>();
  }

  public CompletableFuture<Void> registerWebcam(String camera) {
    var webcam = new Webcam(camera);

    var future = webcam.connect();

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

    for (var webcam : this.webcams) {
      log.debug("Capturing photo from {}", webcam.name);

      photoFutures.add(webcam.getPhoto());
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
}
