package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.executors.BatchGeneratorsExecutor;
import org.laoruga.dtogenerator.generators.executors.ExecutorOfCollectionGenerator;
import org.laoruga.dtogenerator.generators.executors.ExecutorOfDtoDependentGenerator;
import org.laoruga.dtogenerator.generators.executors.ExecutorOfGenerator;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator<T> {

    private final Supplier<Object> dtoInstanceSupplier;
    @Getter(AccessLevel.PACKAGE)
    private final FieldGeneratorsProvider fieldGeneratorsProvider;
    @Getter(AccessLevel.PACKAGE)
    private final FieldGeneratorsHolder fieldGeneratorHolder = new FieldGeneratorsHolder();
    @Getter(AccessLevel.PACKAGE)
    private final DtoGeneratorBuilder<T> builderInstance;
    final ErrorsHolder errorsHolder = new ErrorsHolder();

    ExecutorOfDtoDependentGenerator executorsChain;

    protected DtoGenerator(FieldGeneratorsProvider fieldGeneratorsProvider,
                           DtoGeneratorBuilder<T> dtoGeneratorBuilder) {
        this.fieldGeneratorsProvider = fieldGeneratorsProvider;
        this.builderInstance = dtoGeneratorBuilder;
        this.dtoInstanceSupplier = fieldGeneratorsProvider.getDtoInstanceSupplier();
        this.executorsChain =
                new ExecutorOfDtoDependentGenerator(dtoInstanceSupplier,
                        new ExecutorOfCollectionGenerator(dtoInstanceSupplier,
                                new ExecutorOfGenerator(dtoInstanceSupplier)));
    }

    public static <T> DtoGeneratorBuilder<T> builder(Class<T> dtoClass) {
        return new DtoGeneratorBuilder<>(dtoClass);
    }

    public static <T> DtoGeneratorBuilder<T> builder(T dtoInstance) {
        return new DtoGeneratorBuilder<>(dtoInstance);
    }

    @SuppressWarnings("unchecked")
    public T generateDto() {
        if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
            ((DtoInstanceSupplier) dtoInstanceSupplier).updateInstance();
        }
        prepareGeneratorsRecursively(dtoInstanceSupplier.get().getClass());
        applyGenerators();
        return (T) dtoInstanceSupplier.get();
    }

    private void prepareGeneratorsRecursively(Class<?> dtoClass) {
        if (dtoClass.getSuperclass() != null) {
            prepareGeneratorsRecursively(dtoClass.getSuperclass());
        }
        prepareGenerators(dtoClass);
    }

    void applyGenerators() {

        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getMaxDependentGenerationCycles();

        BatchGeneratorsExecutor batchGeneratorsExecutor = new BatchGeneratorsExecutor(
                executorsChain, getFieldGeneratorHolder(), maxAttempts);

        batchGeneratorsExecutor.execute();
    }

    void prepareGenerators(Class<?> dtoClass) {
            for (Field field : dtoClass.getDeclaredFields()) {
            Optional<IGenerator<?>> generator = Optional.empty();
            try {
                generator = getFieldGeneratorsProvider().getGenerator(field);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            }
            generator.ifPresent(typeGeneratorInstance ->
                    getFieldGeneratorHolder().put(field, typeGeneratorInstance));
        }
        if (!errorsHolder.isEmpty()) {
            log.error("{} error(s) while generators preparation. See problems below: \n"
                    + errorsHolder, errorsHolder.getErrorsNumber());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (getFieldGeneratorHolder().isEmpty()) {
            log.debug("Generators not found");
        } else {
            final AtomicInteger idx = new AtomicInteger(0);
            log.debug(getFieldGeneratorHolder().size() + " generators created for fields: \n" +
                    getFieldGeneratorHolder().getString());
        }
    }

    public static class FieldGeneratorsHolder {
        private int size;
        private final int bucketsNumber;
        private final List<Map<Field, IGenerator<?>>> buckets;
        private int nextIdx = 0;

        public FieldGeneratorsHolder() {
            bucketsNumber = 3;
            buckets = new ArrayList<>(bucketsNumber);
            for (int i = 0; i < bucketsNumber; i++) {
                buckets.add(new HashMap<>());
            }
        }

        public void put(Field field, IGenerator<?> generator) {
            buckets.get(nextIdx()).put(field, generator);
            size++;
        }

        private int nextIdx() {
            if (nextIdx < bucketsNumber) {
                return nextIdx++;
            }
            nextIdx = 0;
            return nextIdx++;
        }

        public boolean isEmpty() {
            return buckets.stream().allMatch(Map::isEmpty);
        }

        public int size() {
            return size;
        }

        public String getString() {
            StringBuilder stringBuilder = new StringBuilder();
            final AtomicInteger idx = new AtomicInteger(0);
            for (Map<Field, IGenerator<?>> bucket : buckets) {
                stringBuilder.append(
                        bucket.keySet().stream()
                                .map(i -> idx.incrementAndGet() + ". " + i)
                                .collect(Collectors.joining("\n"))
                );
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        }

        public List<Map<Field, IGenerator<?>>> getBuckets() {
            return buckets;
        }
    }
}
