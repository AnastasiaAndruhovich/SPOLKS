package by.andruhovich.server.exception.file;

public class FileActionTechnicalException extends Exception {
    public FileActionTechnicalException() {
    }

    public FileActionTechnicalException(String message) {
        super(message);
    }

    public FileActionTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileActionTechnicalException(Throwable cause) {
        super(cause);
    }

    public FileActionTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
