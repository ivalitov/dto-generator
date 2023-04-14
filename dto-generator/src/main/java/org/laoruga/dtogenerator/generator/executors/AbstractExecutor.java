package org.laoruga.dtogenerator.generator.executors;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.Generator;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Getter(AccessLevel.PROTECTED)
abstract class AbstractExecutor {

    private final AbstractExecutor nextExecutor;

    public AbstractExecutor(AbstractExecutor nextExecutor) {
        this.nextExecutor = nextExecutor;
    }

    public AbstractExecutor() {
        this.nextExecutor = DUMMY;
    }

    public abstract boolean execute(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier);

    protected boolean executeNextInstead(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier) {
        return nextExecutor.execute(field, generator, dtoInstanceSupplier);
    }

    private static final AbstractExecutor DUMMY = new AbstractExecutor() {
        @Override
        public boolean execute(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier) {
            throw new IllegalStateException("Next executor haven't defined");
        }
    };
}
