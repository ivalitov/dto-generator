package org.laoruga.dtogenerator.typegenerators.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.ErrorsHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class BatchGeneratorsExecutor {

    private final Map<Field, IGenerator<?>> fieldGeneratorMap;
    private final AbstractExecutor executorsChain;
    private  int maxAttempts;
    private final ErrorsHolder errorsHolder = new ErrorsHolder();

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
            areAllCompleted = executeEachRemaining(attempt);
        }

        if (!checkIfAllGeneratorsExecuted()) {
            log.error("{} error(s) while generators execution. See problems below: \n" + errorsHolder, errorsHolder.size());
            throw new DtoGeneratorException("Error while generators execution");
        }
    }

    private boolean executeEachRemaining(int attempt) {
        Iterator<Map.Entry<Field, IGenerator<?>>> iterator = fieldGeneratorMap.entrySet().iterator();
        while (iterator.hasNext() && maxAttempts > attempt) {
            Map.Entry<Field, IGenerator<?>> nextGenerator = iterator.next();
            Field field = nextGenerator.getKey();

            boolean completed;
            try {
                completed = executorsChain.execute(field, nextGenerator.getValue());
            } catch (Exception e) {
                attempt++;
                errorsHolder.put(field, e);
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
            AtomicInteger counter = new AtomicInteger(0);
            String leftGenerators = fieldGeneratorMap.entrySet().stream()
                    .map((i) -> counter.incrementAndGet() + ". Field: '" + i.getKey() + "', generator: '" + i.getValue() + "'")
                    .collect(Collectors.joining("\n"));
            log.error("Unexpected state. There {} unused generator(s) left:\n" +
                    leftGenerators, fieldGeneratorMap.size());
        }
        if (!errorsHolder.isEmpty()) {
            log.warn("{} error(s) while generators execution:\n" + errorsHolder, errorsHolder.size());
        }
        return successful;
    }

}
