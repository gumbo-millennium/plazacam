package nu.gumbo;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import nu.gumbo.Exceptions.CaptureFailedException;
import nu.gumbo.Models.WebcamConfiguration;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebcamCapturer implements Runnable {

    private final int requestedHeight = 720;

    private final WebcamConfiguration config;
    private Webcam webcam;
    private Boolean capturing = false;

    private static BufferedImage overlay = null;
    private static BufferedImage logo = null;

    private static BufferedImage addOverlays(BufferedImage image) throws Exception
    {
        // TODO load overlay and logo into memory

        // TODO apply overlay to image

        // TODO apply logo to image

        throw new Exception("Nobody finished this!");
    }

    public WebcamCapturer(WebcamConfiguration config) {
        this.config = config;
    }

    @Override
    public void run() {
        if (capturing) {
            return;
        }

        try {
            capturing = true;

            ByteBuffer capture = captureWebcam();

            BufferedImage image = convertToProperScaledImage(capture);

            image = addOverlayAndTitle(image);

            File output = writeToFile(image);
        } catch (InterruptedException exception) {
            System.out.printf("[%s] Task got interrupted\n", config.name);
        } catch (IOException exception) {
            System.out.printf("[%s] Task got IO exception: %s\n", config.name, exception.getLocalizedMessage());
        } catch (CaptureFailedException exception) {
            System.out.printf("[%s] Task got capture exception: %s\n", config.name, exception.getLocalizedMessage());
        } catch (WebcamException exception) {
            System.out.printf("[%s] Got exception from Webcam service: %s\n", config.name, exception.getLocalizedMessage());
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }

            capturing = false;
        }
    }

    private ByteBuffer captureWebcam() throws InterruptedException {
        // Get camera by name if no lock was set
        if (webcam == null) {
            webcam = findWebcam(config.source);

            // Fail if the cam was missing
            if (webcam == null) {
                throw new CaptureFailedException(String.format(
                    "Failed to connect to webcam. Webcam %s not found.",
                    config.source
                ));
            }

            System.out.printf("[%s] Found webcam \"%s\" for [%s]\n", config.name, webcam.getName(), config.source);
        }

        // Find available sizes and use a proper one
        Dimension largestSize = null;
        for (Dimension size : webcam.getViewSizes()) {
            System.out.printf("[%s] Dimension option: %d×%d\n", config.name, (int) size.getWidth(),(int) size.getHeight());

            if (largestSize == null || size.getHeight() > largestSize.getHeight()) {
                largestSize = size;
            }
        }

        // Assign the largest size if we managed to get one
        if (largestSize != null) {
            try {
                webcam.setViewSize(largestSize);
            } catch (WebcamException exception) {
                System.out.printf("[%s] Failed to set webcam size: %s", config.name, exception.getLocalizedMessage());
            }

        }

        // Open the webcam if it's not opened yet or closed
        if (!webcam.isOpen()) {
            System.out.println("Opening new webcam connection");

            webcam.open();
        }

        // Attempt to open 50 times, spanned across 10 seconds.
        int iterationsLeft = 50;
        long sleepDelay = (10 * 1000l) / iterationsLeft;

        while (true) {
            if (webcam.isOpen()) {
                System.out.printf("[%s] Achieved webcam connection\n", config.name);
                break;
            }

            // Allow for a bunch of
            if (iterationsLeft-- <= 0) {
                throw new CaptureFailedException("Cannot open camera");
            }

            System.out.format("Sleeping for %.2ds...\n", sleepDelay / 1000f);

            // Might throw an exception, but we'll deal with that upstream.
            Thread.sleep(sleepDelay);
        }

        // Sleep if the webcam config requests it
        if (config.sleepInterval > 0f) {
            System.out.format("Sleeping for %.2ds...\n", config.sleepInterval);

            Thread.sleep((long) (config.sleepInterval * 1000f));
        }

        System.out.format("Capturing photo");

        // Capture a photo
        return WebcamUtils.getImageByteBuffer(webcam, ImageUtils.FORMAT_JPG);
    }

    private BufferedImage convertToProperScaledImage(ByteBuffer input) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(input.array()));

        // Scale image to always be a certain height
        if (image.getHeight() < requestedHeight) {
            System.out.format("Image has height %dpx, which is small enough\n", image.getHeight());

            return image;
        }

        System.out.format("Image too large at %dpx tall. Scaling to %dpx\n", image.getHeight(), requestedHeight);

        // BufferedImage has an internal method, but the performance is horrible
        // and it leads to incompatible buffers, so we write our own.
        int newWidth = image.getWidth() / image.getHeight() * requestedHeight;
        int newHeight = requestedHeight;

        BufferedImage scaledBuffer = new BufferedImage(
            newWidth, newHeight, image.getType()
        );

        Graphics2D g = scaledBuffer.createGraphics();
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        // Draw the image scaled on the canvas
        g.drawImage(
            image,
            0, 0, newWidth, newHeight,
            0, 0,
            image.getWidth(), image.getHeight(),
            null
        );

        g.dispose();

        return scaledBuffer;
    }

    private BufferedImage addOverlayAndTitle(BufferedImage image)
    {
        // Add overlay


        // Add time

        // Add name
        // Done
        return image;
    }

    private File writeToFile(BufferedImage image) throws IOException {
        Path tempFilePath = Files.createTempFile("image-", ".jpg");
        tempFilePath = Paths.get("/home/roelof/cam.jpg");
        File tempFile = tempFilePath.toFile();

        ImageIO.write(image, "jpg", tempFile);

        System.out.format("Created file [%s], of %d kb\n", tempFile.getAbsolutePath(), tempFile.length() / 1024l);

        return tempFile;
    }

    private Webcam findWebcam(String name)
    {
        // Find by literal name
        Webcam webcam =  Webcam.getWebcamByName(name);
        if (webcam != null) {
            return webcam;
        }

        for (Webcam camOption: Webcam.getWebcams()) {
            if (camOption.getName().contains(name)) {
                return camOption;
            }
        }

        return null;
    }
}
