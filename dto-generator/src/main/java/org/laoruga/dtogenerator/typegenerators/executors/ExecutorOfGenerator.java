package org.laoruga.dtogenerator.typegenerators.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class ExecutorOfGenerator extends AbstractExecutor {

    private final Supplier<?> dtoInstanceSupplier;

    public <T> ExecutorOfGenerator(Supplier<?> dtoInstanceSupplier) {
        super(null);
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }

    // TODO consolidate errors
    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
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
            throw new DtoGeneratorException("Error while setting generated value of type: '" +
                    (result != null ? result.getClass() : null) + "' to the field: " +
                    "'" + field + "'", e);
        }
        return true;
    }
}
