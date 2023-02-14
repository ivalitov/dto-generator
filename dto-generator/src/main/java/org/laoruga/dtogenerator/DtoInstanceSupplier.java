package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
public class DtoInstanceSupplier implements Supplier<Object> {

    private final ThreadLocal<Object> threadLocalDtoInstance = new ThreadLocal<>();
    private final Class<?> dtoClass;

    public DtoInstanceSupplier(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    @Override
    public Object get() {
        if (threadLocalDtoInstance.get() == null) {
            updateInstance();
        }
        return threadLocalDtoInstance.get();
    }

    public void updateInstance() {
        threadLocalDtoInstance.set(ReflectionUtils.createInstance(dtoClass));
    }

    public void remove() {
        threadLocalDtoInstance.remove();
    }
}
