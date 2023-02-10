package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.util.ReflectionUtils;

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
        if (dtoInstance == null) {
            updateInstance();
        }
        return dtoInstance;
    }

    public void updateInstance() {
        dtoInstance = ReflectionUtils.createInstance(dtoClass);
    }

    static class StaticInstance implements Supplier<Object> {

        private final Object dtoInstance;

        public StaticInstance(Object dtoInstance) {
            this.dtoInstance = dtoInstance;
        }

        @Override
        public Object get() {
            return dtoInstance;
        }
    }
}
