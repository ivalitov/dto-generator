package org.laoruga.dtogenerator.generatorsexecutor;

import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;

import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */

public class ExecutorOfCollectionGenerator extends ExecutorOfDtoDependentGenerator {
    public ExecutorOfCollectionGenerator(AbstractExecutor nextGenerators) {
        super(nextGenerators);
    }

    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
        if (generator instanceof ICollectionGenerator) {
            IGenerator<?> innerGenerator = ((ICollectionGenerator<?>) generator).getElementGenerator();
            if (!isDtoReadyForFieldGeneration(innerGenerator)) {
                return false;
            }
        }
        return executeNext(field, generator);
    }
}
