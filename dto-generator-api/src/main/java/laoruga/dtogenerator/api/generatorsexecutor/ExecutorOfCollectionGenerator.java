package laoruga.dtogenerator.api.generatorsexecutor;

import laoruga.dtogenerator.api.markup.generators.ICollectionGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

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
            IGenerator<?> innerGenerator = ((ICollectionGenerator<?>) generator).getItemGenerator();
            if (!isDtoReadyForFieldGeneration(innerGenerator)) {
                return false;
            }
        }
        return executeNext(field, generator);
    }
}
