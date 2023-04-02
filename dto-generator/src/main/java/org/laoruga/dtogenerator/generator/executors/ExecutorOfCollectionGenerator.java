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
    public ExecutorOfCollectionGenerator(Supplier<?> dtoInstanceSupplier,
                                         AbstractExecutor nextGenerator) {
        super(dtoInstanceSupplier, nextGenerator);
    }

    @Override
    public boolean execute(Field field, Generator<?> generator) {
        if (generator instanceof ListGenerator) {

            Generator<?> innerGenerator = ((ListGenerator) generator).getElementGenerator();

            if (isItDtoDependentGenerator(innerGenerator)) {
                return super.execute(field, generator);
            }

            return super.execute(field, generator);
        }

        return executeNextInstead(field, generator);
    }
}
