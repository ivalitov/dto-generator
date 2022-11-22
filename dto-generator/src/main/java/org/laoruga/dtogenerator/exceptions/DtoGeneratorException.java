package org.laoruga.dtogenerator.exceptions;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public class DtoGeneratorException extends RuntimeException {

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
