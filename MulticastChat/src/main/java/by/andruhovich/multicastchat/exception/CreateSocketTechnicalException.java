package by.andruhovich.multicastchat.exception;

public class CreateSocketTechnicalException extends Exception {
    public CreateSocketTechnicalException() {
    }

    public CreateSocketTechnicalException(String message) {
        super(message);
    }

    public CreateSocketTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateSocketTechnicalException(Throwable cause) {
        super(cause);
    }

    public CreateSocketTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
