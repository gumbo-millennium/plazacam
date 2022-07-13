package nl.gumbomillennium.plazacam;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public
class UploadController {

    private final String name;

    public UploadController(String deviceName) {
        name = deviceName;
    }

    public CompletableFuture<Void> upload(CharSequence photo) {
        return CompletableFuture.runAsync(
            () -> {
                System.out.println("Uploading photo to " + name);

                // TODO
            }
        );
    }

    public CompletableFuture<Void> upload(CharSequence[] photos) {
        // Keep track of futures
        var futures = new ArrayList<CompletableFuture<Void>>();

        // Upload photos
        for (var photo : photos) {
            if (photo != null) {
                futures.add(upload(photo));
            }
        }

        // Return as basic future
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
