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
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class BatchGeneratorsExecutor {

    private final Map<Field, IGenerator<?>> generatorMap;
    private final ThreadLocal<Map<Field, IGenerator<?>>> notExecutedGeneratorsMap;
    private final AbstractExecutor executorsChain;
    private final int maxAttempts = DtoGeneratorStaticConfig.getInstance()
            .getDtoGeneratorConfig().getMaxDependentGenerationCycles();
    private final ErrorsHolder errorsHolder;

    public BatchGeneratorsExecutor(AbstractExecutor executorsChain, Map<Field, IGenerator<?>> generatorMap) {
        this.generatorMap = generatorMap;
        this.notExecutedGeneratorsMap = new ThreadLocal<>();
        this.executorsChain = executorsChain;
        this.errorsHolder  = new ErrorsHolder();
    }

    public void execute() {
        try {
            boolean areAllCompleted = generatorMap.isEmpty();

            AtomicInteger attempt = new AtomicInteger(1);

            while (!areAllCompleted && maxAttempts > attempt.get()) {
                areAllCompleted = executeEachRemaining(attempt);
            }

            if (!areAllCompleted) {
                logErrorInfo();
                throw new DtoGeneratorException("Error while generators execution");
            }
        } finally {
            notExecutedGeneratorsMap.remove();
        }
    }

    private boolean executeEachRemaining(AtomicInteger attempt) {
        Map<Field, IGenerator<?>> failedGeneratorsMap = notExecutedGeneratorsMap.get();
        boolean failedGenerations = failedGeneratorsMap != null && !failedGeneratorsMap.isEmpty();

        Iterator<Map.Entry<Field, IGenerator<?>>> generatorsIterator = failedGenerations ?
                failedGeneratorsMap.entrySet().iterator() :
                generatorMap.entrySet().iterator();

        while (generatorsIterator.hasNext() && maxAttempts >= attempt.get()) {
            Map.Entry<Field, IGenerator<?>> nextGenerator = generatorsIterator.next();
            Field field = nextGenerator.getKey();
            IGenerator<?> generator = nextGenerator.getValue();

            boolean completed = false;
            try {
                completed = executorsChain.execute(field, generator);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            } finally {
                if (!completed) {
                    attempt.incrementAndGet();
                    if (failedGeneratorsMap == null) {
                        failedGeneratorsMap = new HashMap<>();
                        notExecutedGeneratorsMap.set(failedGeneratorsMap);
                    }
                    failedGeneratorsMap.put(field, generator);
                }
                if (completed && failedGenerations) {
                    generatorsIterator.remove();
                }
            }

        }
        return failedGeneratorsMap == null || failedGeneratorsMap.isEmpty();
    }

    private void logErrorInfo() {
        Map<Field, IGenerator<?>> fieldIGeneratorMap = notExecutedGeneratorsMap.get();
        AtomicInteger counter = new AtomicInteger(0);
        String leftGenerators = fieldIGeneratorMap.entrySet().stream()
                .map((i) -> counter.incrementAndGet() + ". Field: '" + i.getKey() + "', generator: '" + i.getValue() + "'")
                .collect(Collectors.joining("\n"));

        log.error("Unsuccessful generation. {} error(s) while generators execution. See problems below:\n{}",
                errorsHolder.getErrorsNumber(), errorsHolder);

        log.error("Unexpected state. There {} unused generator(s) left:\n{}",
                fieldIGeneratorMap.size(), leftGenerators);
    }
}
