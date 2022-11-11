package laoruga.dtogenerator.api.generatorsexecutor;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class BatchGeneratorsExecutor {

    private Map<Field, IGenerator<?>> fieldGeneratorMap;
    private final AbstractExecutor executorsChain;
    private final int maxAttempts;

    private Map<Field, Exception> errors = new HashMap<>();

    public BatchGeneratorsExecutor(AbstractExecutor executorsChain,
                                   Map<Field, IGenerator<?>> fieldGeneratorMap,
                                   int maxAttempts) {
        this.fieldGeneratorMap = fieldGeneratorMap;
        this.executorsChain = executorsChain;
        this.maxAttempts = maxAttempts;
    }

    public void execute() {

        boolean areAllCompleted = fieldGeneratorMap.isEmpty();

        int attempt = 0;

        while (!areAllCompleted && maxAttempts > attempt) {
            attempt++;
            areAllCompleted = executeEachRemaining();
        }

        if (!checkIfAllGeneratorsExecuted()) {
            throw new DtoGeneratorException("Error while generators execution (see log above)");
        }
    }

    private boolean executeEachRemaining() {
        Iterator<Map.Entry<Field, IGenerator<?>>> iterator = fieldGeneratorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Field, IGenerator<?>> nextGenerator = iterator.next();

            boolean completed;
            try {
                completed = executorsChain.execute(nextGenerator.getKey(), nextGenerator.getValue());
            } catch (Exception e) {
                errors.put(nextGenerator.getKey(), e);
                completed = false;
            }
            if (completed) {
                iterator.remove();
            }
        }
        return fieldGeneratorMap.isEmpty();
    }

    private boolean checkIfAllGeneratorsExecuted() {
        boolean successful = fieldGeneratorMap.isEmpty();
        if (!successful) {
            log.error("Unexpected state. There {} unused generator(s) left. Fields vs Generators: " +
                    fieldGeneratorMap, fieldGeneratorMap.size());
        }
        if (!errors.isEmpty()) {
            log.warn("{} error(s) while generators execution. Fields vs Generators: " + errors, errors.size());
        }
        return successful;
    }

}
