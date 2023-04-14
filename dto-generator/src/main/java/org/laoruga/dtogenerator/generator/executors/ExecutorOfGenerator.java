package org.laoruga.dtogenerator.generator.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class ExecutorOfGenerator extends AbstractExecutor {

    public ExecutorOfGenerator() {
        super();
    }

    public ExecutorOfGenerator(AbstractExecutor nextExecutor) {
        super(nextExecutor);
    }

    @Override
    public boolean execute(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier) {
        Object result;
        try {
            result = generator.generate();
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while generating value for the field: " + field, e);
        }
        try {
            field.setAccessible(true);
            field.set(dtoInstanceSupplier.get(), result);
        } catch (Exception e) {
            throw new DtoGeneratorException("Unable to set generated value of type: '" +
                    (result != null ? result.getClass() : null) + "' to the field: " +
                    "'" + field.getName() + "'. field type: '" + field.getType() + "'", e);
        }
        return true;
    }
}
