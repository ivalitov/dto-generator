package org.laoruga.dtogenerator.generators.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.ErrorsHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class BatchGeneratorsExecutor {

    private final DtoGenerator.FieldGeneratorsHolder fieldGeneratorMap;
    private final AbstractExecutor executorsChain;
    private int maxAttempts;
    private final ErrorsHolder errorsHolder = new ErrorsHolder();

    static ExecutorService executorService = Executors.newCachedThreadPool();

    public BatchGeneratorsExecutor(AbstractExecutor executorsChain,
                                   DtoGenerator.FieldGeneratorsHolder fieldGeneratorHolder,
                                   int maxAttempts) {
        this.fieldGeneratorMap = fieldGeneratorHolder;
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
            log.error("{} error(s) while generators execution. See problems below: \n" + errorsHolder, errorsHolder.getErrorsNumber());
            throw new DtoGeneratorException("Error while generators execution");
        }
    }

    private boolean executeEachRemaining(int attempt) {
        AtomicInteger attemptsAtomic = new AtomicInteger(attempt);

        try {
            List<Future<Boolean>> futures = executorService.invokeAll(
                    fieldGeneratorMap.getBuckets()
                            .stream().map(bucket -> getTask(bucket, attemptsAtomic))
                            .collect(Collectors.toList())
            );
            boolean result = true;
            for (Future<Boolean> future : futures) {
                result &= future.get(5, TimeUnit.MINUTES);
            }
            return result;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private Callable<Boolean> getTask(Map<Field, IGenerator<?>> fieldGeneratorMap, AtomicInteger attemptsAtomic) {
        Iterator<Map.Entry<Field, IGenerator<?>>> iterator = fieldGeneratorMap.entrySet().iterator();
        return () -> {
            while (iterator.hasNext() && maxAttempts > attemptsAtomic.get()) {
                Map.Entry<Field, IGenerator<?>> nextGenerator = iterator.next();
                Field field = nextGenerator.getKey();

                boolean completed;

                try {
                    completed = executorsChain.execute(field, nextGenerator.getValue());
                } catch (Exception e) {
                    attemptsAtomic.incrementAndGet();
                    errorsHolder.put(field, e);
                    completed = false;
                }

                if (completed) {
                    iterator.remove();
                }
            }
            return fieldGeneratorMap.isEmpty();
        };
    }

    private boolean checkIfAllGeneratorsExecuted() {
        boolean successful = fieldGeneratorMap.isEmpty();
        if (!successful) {
            String generatorsLeft = fieldGeneratorMap.getString();
            log.error("Unexpected state. There {} unused generator(s) left:\n" +
                    generatorsLeft, fieldGeneratorMap.size());
        }
        if (!errorsHolder.isEmpty()) {
            log.warn("{} error(s) while generators execution:\n" + errorsHolder, errorsHolder.getErrorsNumber());
        }
        return successful;
    }

}
