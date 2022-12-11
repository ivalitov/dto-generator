package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.typegenerators.executors.BatchGeneratorsExecutor;
import org.laoruga.dtogenerator.typegenerators.executors.ExecutorOfCollectionGenerator;
import org.laoruga.dtogenerator.typegenerators.executors.ExecutorOfDtoDependentGenerator;
import org.laoruga.dtogenerator.typegenerators.executors.ExecutorOfGenerator;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator<T> {

    private final Supplier<T> dtoInstanceSupplier;
    @Getter(AccessLevel.PACKAGE)
    private final TypeGeneratorsProvider<T> typeGeneratorsProvider;
    @Getter(AccessLevel.PACKAGE)
    private final Map<Field, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();
    @Getter(AccessLevel.PACKAGE)
    private final DtoGeneratorBuilder<T> builderInstance;
    private final ErrorsHolder errorsHolder = new ErrorsHolder();

    protected DtoGenerator(TypeGeneratorsProvider<T> typeGeneratorsProvider,
                           DtoGeneratorBuilder<T> dtoGeneratorBuilder) {
        this.typeGeneratorsProvider = typeGeneratorsProvider;
        this.builderInstance = dtoGeneratorBuilder;
        this.dtoInstanceSupplier = typeGeneratorsProvider.getDtoInstanceSupplier();
    }

    public static <T> DtoGeneratorBuilder<T> builder(Class<T> dtoClass) {
        return new DtoGeneratorBuilder<>(dtoClass);
    }

    public static <T> DtoGeneratorBuilder<T> builder(T dtoInstance) {
        return new DtoGeneratorBuilder<>(dtoInstance);
    }

    public T generateDto() {
        if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
            ((DtoInstanceSupplier<T>) dtoInstanceSupplier).updateInstance();
        }
        prepareGeneratorsRecursively(dtoInstanceSupplier.get().getClass());
        applyGenerators();
        return dtoInstanceSupplier.get();
    }

    private void prepareGeneratorsRecursively(Class<?> dtoClass) {
        if (dtoClass.getSuperclass() != null) {
            prepareGeneratorsRecursively(dtoClass.getSuperclass());
        }
        prepareGenerators(dtoClass);
    }

    void applyGenerators() {

        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getMaxDependentGenerationCycles();

        ExecutorOfDtoDependentGenerator executorsChain =
                new ExecutorOfDtoDependentGenerator(
                        new ExecutorOfCollectionGenerator(
                                new ExecutorOfGenerator(dtoInstanceSupplier)));

        BatchGeneratorsExecutor batchGeneratorsExecutor = new BatchGeneratorsExecutor(
                executorsChain, getFieldGeneratorMap(), maxAttempts);

        batchGeneratorsExecutor.execute();
    }

    void prepareGenerators(Class<?> dtoClass) {
        for (Field field : dtoClass.getDeclaredFields()) {
            Optional<IGenerator<?>> generator = Optional.empty();
            try {
                generator = getTypeGeneratorsProvider().getGenerator(field);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            }
            generator.ifPresent(typeGeneratorInstance -> getFieldGeneratorMap().put(field, typeGeneratorInstance));
        }
        if (!errorsHolder.isEmpty()) {
            log.error("{} error(s) while generators preparation. See problems below: \n" + errorsHolder, errorsHolder.size());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (getFieldGeneratorMap().isEmpty()) {
            log.debug("Generators not found");
        } else {
            final AtomicInteger idx = new AtomicInteger(0);
            log.debug(getFieldGeneratorMap().size() + " generators created for fields: \n" +
                    getFieldGeneratorMap().keySet().stream()
                            .map(i -> idx.incrementAndGet() + ". " + i)
                            .collect(Collectors.joining("\n")));
        }
    }
}
