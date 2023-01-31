package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
public class DtoInstanceSupplier implements Supplier<Object> {

    private Object dtoInstance;
    private final Class<?> dtoClass;

    public DtoInstanceSupplier(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    @Override
    public Object get() {
        return Objects.requireNonNull(dtoInstance, "DTO instance is null");
    }

    public void updateInstance() {
        dtoInstance = ReflectionUtils.createInstance(dtoClass);
    }
}
