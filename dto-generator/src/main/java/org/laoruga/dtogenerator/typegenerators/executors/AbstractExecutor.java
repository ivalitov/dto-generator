package org.laoruga.dtogenerator.typegenerators.executors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;

import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractExecutor {

    private final AbstractExecutor nextExecutor;


    public AbstractExecutor(AbstractExecutor nextExecutor) {
        this.nextExecutor = nextExecutor;
    }

    public AbstractExecutor() {
        this.nextExecutor = new AbstractExecutor(null) {
            @Override
            public boolean execute(Field field, IGenerator<?> generator) {
                throw new IllegalStateException("There is no next executor defined");
            }
        };
    }

    public abstract boolean execute(Field field, IGenerator<?> generator);

    protected boolean executeNext(Field field, IGenerator<?> generator) {
        return nextExecutor.execute(field, generator);
    }
}
