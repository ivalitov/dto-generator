package org.laoruga.dtogenerator.generator.executors;

import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.ListGenerator;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */

public class ExecutorOfCollectionGenerator extends ExecutorOfDtoDependentGenerator {

    public ExecutorOfCollectionGenerator(AbstractExecutor nextGenerator) {
        super(nextGenerator);
    }

    @Override
    public boolean execute(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier) {
        if (generator instanceof ListGenerator) {

            Generator<?> innerGenerator = ((ListGenerator) generator).getElementGenerator();

            if (isItDtoDependentGenerator(innerGenerator)) {
                return super.execute(field, generator, dtoInstanceSupplier);
            }

            return super.execute(field, generator, dtoInstanceSupplier);
        }

        return executeNextInstead(field, generator, dtoInstanceSupplier);
    }
}
