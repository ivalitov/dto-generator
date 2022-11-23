package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.DtoGeneratorConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generatorsexecutor.BatchGeneratorsExecutor;
import org.laoruga.dtogenerator.generatorsexecutor.ExecutorOfCollectionGenerator;
import org.laoruga.dtogenerator.generatorsexecutor.ExecutorOfDtoDependentGenerator;
import org.laoruga.dtogenerator.generatorsexecutor.ExecutorOfGenerator;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator<T> {

    private final T dtoInstance;

    @Getter(AccessLevel.PACKAGE)
    private final GeneratorsProvider<T> generatorsProvider;
    @Getter(AccessLevel.PACKAGE)
    private final Map<Field, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();
    @Getter(AccessLevel.PACKAGE)
    private final DtoGeneratorBuilder<T> builderInstance;
    private final Map<Field, Exception> errors = new HashMap<>();

    protected DtoGenerator(GeneratorsProvider<T> generatorsProvider,
                           DtoGeneratorBuilder<T> dtoGeneratorBuilder) {
        this.generatorsProvider = generatorsProvider;
        this.builderInstance = dtoGeneratorBuilder;
        this.dtoInstance = generatorsProvider.getDtoInstance();
    }

    public static <T> DtoGeneratorBuilder<T> builder(Class<T> dtoClass) {
        return new DtoGeneratorBuilder<>(ReflectionUtils.createInstance(dtoClass));
    }

    public static <T> DtoGeneratorBuilder<T> builder(T dtoInstance) {
        return new DtoGeneratorBuilder<>(dtoInstance);
    }

    public T generateDto() {
        prepareGeneratorsRecursively(dtoInstance.getClass());
        applyGenerators();
        return dtoInstance;
    }

    private void prepareGeneratorsRecursively(Class<?> dtoClass) {
        if (dtoClass.getSuperclass() != null) {
            prepareGeneratorsRecursively(dtoClass.getSuperclass());
        }
        prepareGenerators(dtoClass);
    }

    void applyGenerators() {

        int maxAttempts = DtoGeneratorConfig.getMaxDependentGenerationCycles();

        ExecutorOfDtoDependentGenerator executorsChain =
                new ExecutorOfDtoDependentGenerator(
                        new ExecutorOfCollectionGenerator(
                                new ExecutorOfGenerator(dtoInstance)));

        BatchGeneratorsExecutor batchGeneratorsExecutor = new BatchGeneratorsExecutor(
                executorsChain, getFieldGeneratorMap(), maxAttempts);

        batchGeneratorsExecutor.execute();
    }

    void prepareGenerators(Class<?> dtoClass) {
        for (Field field : dtoClass.getDeclaredFields()) {
            Optional<IGenerator<?>> generator = Optional.empty();
            try {
                generator = getGeneratorsProvider().getGenerator(field);
            } catch (Exception e) {
                errors.put(field, e);
            }
            generator.ifPresent(iGenerator -> getFieldGeneratorMap().put(field, iGenerator));
        }
        if (!errors.isEmpty()) {
            final AtomicInteger counter = new AtomicInteger(0);
            String formattedErrors = errors.entrySet().stream()
                    .map(fieldExceptionEntry ->
                            "- [" + counter.incrementAndGet() + "] Field: '" + fieldExceptionEntry.getKey().toString() + "'\n" +
                                    "- [" + counter.get() + "] Exception:\n" +
                                    ExceptionUtils.getStackTrace(fieldExceptionEntry.getValue())
                    )
                    .collect(Collectors.joining("\n"));
            log.error("{} error(s) while generators preparation. See problems below: \n" + formattedErrors, errors.size());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (getFieldGeneratorMap().isEmpty()) {
            log.debug("No generators have been found");
        } else {
            log.debug(getFieldGeneratorMap().size() + " generators was created for fields: " + getFieldGeneratorMap().keySet());
        }
    }
}
