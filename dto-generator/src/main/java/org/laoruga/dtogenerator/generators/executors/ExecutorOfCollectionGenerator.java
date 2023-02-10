package org.laoruga.dtogenerator.generators.executors;

import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */

public class ExecutorOfCollectionGenerator extends ExecutorOfDtoDependentGenerator {
    public ExecutorOfCollectionGenerator(ThreadLocal<Supplier<?>> dtoInstanceSupplier,
                                         AbstractExecutor nextGenerator) {
        super(dtoInstanceSupplier, nextGenerator);
    }

    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
        if (generator instanceof ICollectionGenerator) {

            IGenerator<?> innerGenerator = ((ICollectionGenerator<?>) generator).getElementGenerator();

            if (isItDtoDependentGenerator(innerGenerator)) {
                if (isDtoReadyForFieldGeneration(innerGenerator)) {
                    return super.execute(field, generator);
                }
                return false;
            }
            return super.execute(field, generator);
        }
        return executeNext(field, generator);
    }
}
