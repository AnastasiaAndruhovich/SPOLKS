package by.andruhovich.client.exception.socket;

public class SentAttemptTechnicalException extends Exception {
    public SentAttemptTechnicalException() {
    }

    public SentAttemptTechnicalException(String message) {
        super(message);
    }

    public SentAttemptTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public SentAttemptTechnicalException(Throwable cause) {
        super(cause);
    }

    public SentAttemptTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
