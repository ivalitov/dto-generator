package org.laoruga.dtogenerator.generator.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.ErrorsHolder;
import org.laoruga.dtogenerator.FieldGenerators;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Thread safe batch generators executor.
 *
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class BatchExecutor {

    private final FieldGenerators fieldGenerators;
    private final ThreadLocal<FieldGenerators> generatorsNotExecutedDueToError;
    private final AbstractExecutor executorsChain;
    private final ErrorsHolder errorsHolder;
    private int maxAttempts;

    public BatchExecutor(AbstractExecutor executorsChain, FieldGenerators fieldGenerators) {
        this.fieldGenerators = fieldGenerators;
        this.generatorsNotExecutedDueToError = new ThreadLocal<>();
        this.executorsChain = executorsChain;
        this.errorsHolder = new ErrorsHolder();
    }

    public void execute() {
        try {

            maxAttempts = DtoGeneratorStaticConfig.getInstance()
                    .getDtoGeneratorConfig()
                    .getMaxDependentGenerationCycles();

            boolean success = fieldGenerators.isEmpty();

            AtomicInteger attempt = new AtomicInteger(1);

            while (!success && maxAttempts > attempt.get()) {

                FieldGenerators failedGenerators = generatorsNotExecutedDueToError.get();

                if (failedGenerators == null || failedGenerators.isEmpty()) {
                    success = executeEachGenerator(attempt);
                } else {
                    success = executeEachRemainingGenerator(attempt);
                }

            }

            if (!success) {
                logErrorInfo();
                throw new DtoGeneratorException("All attempts to generate field values have been exhausted," +
                        " but not all fields have been set. See details for every not set field above.");
            }

        } finally {
            generatorsNotExecutedDueToError.remove();
        }

    }

    private boolean executeEachGenerator(AtomicInteger attempt) {

        boolean allGeneratorExecutedSuccessfully = true;

        if (fieldGenerators.isNestedFieldsExist()) {
            executeNestedDtoGenerators();
        }

        Iterator<Map.Entry<Supplier<?>, FieldGenerators.GeneratorEntry>> filedGeneratorsIterator =
                fieldGenerators.getFieldGeneratorsMap().entrySet().iterator();

        while (filedGeneratorsIterator.hasNext() && maxAttempts >= attempt.get()) {

            Map.Entry<Supplier<?>, FieldGenerators.GeneratorEntry> nextGenerator = filedGeneratorsIterator.next();

            Iterator<Map.Entry<Field, Generator<?>>> fieldGeneratorsIterator = nextGenerator.getValue()
                    .getFieldGeneratorMap().entrySet().iterator();

            Supplier<?> dtoInstanceSupplier = nextGenerator.getKey();

            while (fieldGeneratorsIterator.hasNext() && maxAttempts >= attempt.get()) {

                boolean generatorExecutedSuccessfully = false;

                Map.Entry<Field, Generator<?>> next = fieldGeneratorsIterator.next();

                Field field = next.getKey();
                Generator<?> generator = next.getValue();

                try {
                    generatorExecutedSuccessfully = executorsChain.execute(field, generator, dtoInstanceSupplier);
                } catch (Exception e) {
                    errorsHolder.put(field, e);
                } finally {
                    if (!generatorExecutedSuccessfully) {
                        allGeneratorExecutedSuccessfully = false;
                        attempt.incrementAndGet();
                        addGeneratorToReExecution(field, generator, dtoInstanceSupplier);
                    }
                }

            }

        }

        return allGeneratorExecutedSuccessfully;
    }

    private boolean executeEachRemainingGenerator(AtomicInteger attempt) {

        FieldGenerators failedGenerators = generatorsNotExecutedDueToError.get();

        Iterator<Map.Entry<Supplier<?>, FieldGenerators.GeneratorEntry>> generatorsIterator =
                failedGenerators.getFieldGeneratorsMap().entrySet().iterator();

        while (generatorsIterator.hasNext() && maxAttempts >= attempt.get()) {
            Map.Entry<Supplier<?>, FieldGenerators.GeneratorEntry> nextGenerator = generatorsIterator.next();

            Iterator<Map.Entry<Field, Generator<?>>> fieldGeneratorsIterator = nextGenerator.getValue()
                    .getFieldGeneratorMap().entrySet().iterator();

            Supplier<?> dtoInstanceSupplier = nextGenerator.getKey();

            boolean layerGeneratorsExecutedSuccessfully = true;

            while (fieldGeneratorsIterator.hasNext() && maxAttempts >= attempt.get()) {

                boolean generatorExecutedSuccessfully = false;

                Map.Entry<Field, Generator<?>> next = fieldGeneratorsIterator.next();

                Field field = next.getKey();
                Generator<?> generator = next.getValue();

                try {
                    generatorExecutedSuccessfully = executorsChain.execute(field, generator, dtoInstanceSupplier);
                } catch (Exception e) {
                    errorsHolder.put(field, e);
                } finally {

                    layerGeneratorsExecutedSuccessfully &= generatorExecutedSuccessfully;

                    if (generatorExecutedSuccessfully) {
                        fieldGeneratorsIterator.remove();
                    } else {
                        attempt.incrementAndGet();
                    }

                }

            }

            if (layerGeneratorsExecutedSuccessfully) {
                generatorsIterator.remove();
            }
        }

        return failedGenerators.isEmpty();
    }

    private void executeNestedDtoGenerators() {
        try {
            for (FieldGenerators.NestedGeneratorEntry nestedEntry : fieldGenerators.getNestedDtoGenerators()) {
                executorsChain.execute(
                        nestedEntry.getField(),
                        nestedEntry.getNestedDtoGenerator(),
                        nestedEntry.getDtoInstanceSupplier()
                );
            }
        } catch (Exception e) {
            throw new DtoGeneratorException("Unexpected error during creating instances of nested DTO.", e);
        }
    }

    private void addGeneratorToReExecution(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier) {
        FieldGenerators generatorsToReExecution = generatorsNotExecutedDueToError.get();
        if (generatorsToReExecution == null) {
            generatorsToReExecution = new FieldGenerators();
            generatorsNotExecutedDueToError.set(generatorsToReExecution);
        }
        generatorsToReExecution.addGenerator(field, generator, dtoInstanceSupplier);
    }


    private void logErrorInfo() {
        log.error("Unsuccessful generation. {} error(s) while generators execution. See problems below:\n{}",
                errorsHolder.getErrorsNumber(), errorsHolder);

        FieldGenerators fieldIGeneratorMap = generatorsNotExecutedDueToError.get();

        log.error("Unexpected state. There {} unused generator(s) left:\n{}", fieldIGeneratorMap.size(), fieldIGeneratorMap);
    }
}
