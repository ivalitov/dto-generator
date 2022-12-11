package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
public class DtoInstanceSupplier<T> implements Supplier<T> {

    private T dtoInstance;
    private final Class<T> dtoClass;

    public DtoInstanceSupplier(Class<T> dtoClass) {
        this.dtoClass = dtoClass;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(dtoInstance, "DTO instance is null");
    }

    public void updateInstance() {
        dtoInstance = ReflectionUtils.createInstance(dtoClass);
    }
}
