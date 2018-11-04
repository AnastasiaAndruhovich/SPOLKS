package by.andruhovich.client.exception.socket;

public class AttemptCreateSocketTechnicalException extends Exception {
    public AttemptCreateSocketTechnicalException() {
    }

    public AttemptCreateSocketTechnicalException(String message) {
        super(message);
    }

    public AttemptCreateSocketTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttemptCreateSocketTechnicalException(Throwable cause) {
        super(cause);
    }

    public AttemptCreateSocketTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
