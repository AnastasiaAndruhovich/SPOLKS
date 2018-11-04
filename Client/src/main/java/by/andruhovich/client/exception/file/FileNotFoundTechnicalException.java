package by.andruhovich.client.exception.file;

public class FileNotFoundTechnicalException extends Exception{
    public FileNotFoundTechnicalException() {
    }

    public FileNotFoundTechnicalException(String message) {
        super(message);
    }

    public FileNotFoundTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundTechnicalException(Throwable cause) {
        super(cause);
    }

    public FileNotFoundTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
