package laoruga.dtogenerator.api.generatorsexecutor;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
        boolean isFieldAccessible = field.isAccessible();
        try {
            if (!isFieldAccessible) field.setAccessible(true);
            field.set(dtoInstance, generator.generate());
        } catch (IllegalAccessException e) {
            log.error("Access error while generation value for a field: " + field, e);
        } catch (Exception e) {
            log.error("Error while generation value for the field: " + field, e);
        }
        return true;
    }
}
