package org.laoruga.dtogenerator.generatorsexecutor;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;

import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class ExecutorOfGenerator extends AbstractExecutor {

    private final Object dtoInstance;

    public <T> ExecutorOfGenerator(T dtoInstance) {
        super(null);
        this.dtoInstance = dtoInstance;
    }

    // TODO consolidate errors
    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
        try {
            field.setAccessible(true);
            field.set(dtoInstance, generator.generate());
        } catch (IllegalAccessException e) {
            log.error("Access error while generation value for a field: " + field, e);
        } catch (Exception e) {
            log.error("Error while generation value for the field: " + field, e);
        }
        return true;
    }
}
