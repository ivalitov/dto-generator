package laoruga.dtogenerator.api.exceptions;

public class DtoGeneratorException extends RuntimeException {
    public DtoGeneratorException() {
    }

    public DtoGeneratorException(String message) {
        super(message);
    }

    public DtoGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DtoGeneratorException(Throwable cause) {
        super(cause);
    }

    public DtoGeneratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
