package org.laoruga.dtogenerator.generator.executors;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGenerator;

import java.lang.reflect.Field;

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

    public abstract boolean execute(Field field, IGenerator<?> generator);

    protected boolean executeNextInstead(Field field, IGenerator<?> generator) {
        return nextExecutor.execute(field, generator);
    }

    private static final AbstractExecutor DUMMY = new AbstractExecutor() {
        @Override
        public boolean execute(Field field, IGenerator<?> generator) {
            throw new IllegalStateException("Next executor haven't defined");
        }
    };
}
