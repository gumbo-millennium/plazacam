package nu.gumbo.Exceptions;

public class CaptureFailedException extends RuntimeException {

    private final String message;

    public CaptureFailedException(String message) {
        super();

        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }
}
