package org.laoruga.dtogenerator.generator.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.ErrorsHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread safe batch generators executor.
 *
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class BatchExecutor {

    private final Map<Field, IGenerator<?>> fieldGeneratorMap;
    private final ThreadLocal<Map<Field, IGenerator<?>>> generatorsNotExecutedDueToError;
    private final AbstractExecutor executorsChain;
    private final ErrorsHolder errorsHolder;
    private int maxAttempts;

    public BatchExecutor(AbstractExecutor executorsChain, Map<Field, IGenerator<?>> generatorMap) {
        this.fieldGeneratorMap = generatorMap;
        this.generatorsNotExecutedDueToError = new ThreadLocal<>();
        this.executorsChain = executorsChain;
        this.errorsHolder = new ErrorsHolder();
    }

    public void execute() {
        try {

            maxAttempts = DtoGeneratorStaticConfig.getInstance()
                    .getDtoGeneratorConfig()
                    .getMaxDependentGenerationCycles();

            boolean success = fieldGeneratorMap.isEmpty();

            AtomicInteger attempt = new AtomicInteger(1);

            while (!success && maxAttempts > attempt.get()) {

                Map<Field, IGenerator<?>> failedGeneratorsMap = generatorsNotExecutedDueToError.get();

                if (failedGeneratorsMap == null || failedGeneratorsMap.isEmpty()) {
                    success = executeEachGenerator(attempt);
                } else {
                    success = executeEachRemainingGenerator(attempt);
                }

            }

            if (!success) {
                logErrorInfo();
                throw new DtoGeneratorException("After all attempts, there are not generated field left." +
                        " See details for every not completed field above.");
            }

        } finally {
            generatorsNotExecutedDueToError.remove();
        }
    }

    private boolean executeEachGenerator(AtomicInteger attempt) {

        boolean allGeneratorExecutedSuccessfully = true;

        Iterator<Map.Entry<Field, IGenerator<?>>> generatorsIterator = fieldGeneratorMap.entrySet().iterator();

        while (generatorsIterator.hasNext() && maxAttempts >= attempt.get()) {
            Map.Entry<Field, IGenerator<?>> nextGenerator = generatorsIterator.next();

            Field field = nextGenerator.getKey();
            IGenerator<?> generator = nextGenerator.getValue();

            boolean generatorExecutedSuccessfully = false;
            try {
                generatorExecutedSuccessfully = executorsChain.execute(field, generator);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            } finally {
                if (!generatorExecutedSuccessfully) {
                    allGeneratorExecutedSuccessfully = false;
                    attempt.incrementAndGet();
                    addGeneratorToReExecution(field, generator);
                }
            }
        }

        return allGeneratorExecutedSuccessfully;
    }

    private boolean executeEachRemainingGenerator(AtomicInteger attempt) {

        Map<Field, IGenerator<?>> fieldGeneratorMapNotExecutedDueToError = generatorsNotExecutedDueToError.get();

        Iterator<Map.Entry<Field, IGenerator<?>>> generatorsIterator =
                fieldGeneratorMapNotExecutedDueToError.entrySet().iterator();

        while (generatorsIterator.hasNext() && maxAttempts >= attempt.get()) {
            Map.Entry<Field, IGenerator<?>> nextGenerator = generatorsIterator.next();

            Field field = nextGenerator.getKey();
            IGenerator<?> generator = nextGenerator.getValue();

            boolean generatorExecutedSuccessfully = false;
            try {
                generatorExecutedSuccessfully = executorsChain.execute(field, generator);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            } finally {

                if (generatorExecutedSuccessfully) {
                    generatorsIterator.remove();
                } else {
                    attempt.incrementAndGet();
                }

            }
        }

        return fieldGeneratorMapNotExecutedDueToError.isEmpty();
    }

    private void addGeneratorToReExecution(Field field, IGenerator<?> generator) {
        Map<Field, IGenerator<?>> generatorsToReExecution = generatorsNotExecutedDueToError.get();
        if (generatorsToReExecution == null) {
            generatorsToReExecution = new HashMap<>();
            generatorsNotExecutedDueToError.set(generatorsToReExecution);
        }
        generatorsToReExecution.put(field, generator);
    }


    private void logErrorInfo() {
        Map<Field, IGenerator<?>> fieldIGeneratorMap = generatorsNotExecutedDueToError.get();
        AtomicInteger counter = new AtomicInteger(0);
        String leftGenerators = fieldIGeneratorMap.entrySet()
                .stream()
                .map((i) -> counter.incrementAndGet() + ". Field: '" + i.getKey() + "', generator: '" + i.getValue() + "'")
                .collect(Collectors.joining("\n"));

        log.error("Unsuccessful generation. {} error(s) while generators execution. See problems below:\n{}", errorsHolder.getErrorsNumber(), errorsHolder);

        log.error("Unexpected state. There {} unused generator(s) left:\n{}", fieldIGeneratorMap.size(), leftGenerators);
    }
}
