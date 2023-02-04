package org.laoruga.dtogenerator;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 05.12.2022
 */
public class ErrorsHolder {

    private Map<Field, Exception> errors;

    private Map<Field, Exception> getErrors() {
        if (errors == null) {
            errors = new LinkedHashMap<>();
        }
        return errors;
    }

    public boolean isEmpty() {
        return errors == null || getErrors().isEmpty();
    }

    public void put(Field field, Exception e) {
        getErrors().put(field, e);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        final AtomicInteger counter = new AtomicInteger(0);
        return getErrors().entrySet().stream()
                .map(fieldExceptionEntry ->
                        "- [" + counter.incrementAndGet() + "] Field: '" + fieldExceptionEntry.getKey().toString() + "'\n" +
                                "- [" + counter.get() + "] Exception:\n" +
                                ExceptionUtils.getStackTrace(fieldExceptionEntry.getValue())
                )
                .collect(Collectors.joining("\n"));
    }

    public int getErrorsNumber() {
        return errors == null ? 0 : getErrors().size();
    }
}
