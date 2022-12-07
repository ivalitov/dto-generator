package org.laoruga.dtogenerator;

import lombok.Getter;
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

    @Getter(lazy = true)
    private final Map<Field, Exception> errors = new LinkedHashMap<>();

    public boolean isEmpty() {
        return getErrors().isEmpty();
    }

    public void put(Field field, Exception e) {
        getErrors().put(field, e);
    }

    @Override
    public String toString() {
        final AtomicInteger counter = new AtomicInteger(0);
        return getErrors().entrySet().stream()
                .map(fieldExceptionEntry ->
                        "- [" + counter.incrementAndGet() + "] Field: '" + fieldExceptionEntry.getKey().toString() + "'\n" +
                                "- [" + counter.get() + "] Exception:\n" +
                                ExceptionUtils.getStackTrace(fieldExceptionEntry.getValue())
                )
                .collect(Collectors.joining("\n"));
    }

    public int size() {
        return getErrors().size();
    }
}
