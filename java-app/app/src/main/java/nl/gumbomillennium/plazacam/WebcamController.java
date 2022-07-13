package nl.gumbomillennium.plazacam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class WebcamController {

    private final Collection<Webcam> webcams;

    public WebcamController() {
        this.webcams = Collections.emptyList();
    }

    public void registerWebcam(String camera) {
        var webcam = new Webcam(camera);

        //noinspection ConstantConditions
        this.webcams.add(webcam);
    }

    public CompletableFuture<CharSequence[]> capture() {
        var photoFutures = new ArrayList<CompletableFuture<CharSequence>>();

        for (var webcam : this.webcams) {
            photoFutures.add(webcam.getPhoto());
        }

        var photoFuturesAsArray = photoFutures.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(photoFuturesAsArray).thenApply(v -> {
            var photos = new CharSequence[photoFuturesAsArray.length];

            for (var photoFuture : photoFutures) {
                photos[photoFutures.indexOf(photoFuture)] = photoFuture.isCompletedExceptionally() ? null : photoFuture.join();
            }

            return photos;
        });
    }
}
