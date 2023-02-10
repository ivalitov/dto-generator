package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.executors.BatchGeneratorsExecutor;
import org.laoruga.dtogenerator.generators.executors.ExecutorOfCollectionGenerator;
import org.laoruga.dtogenerator.generators.executors.ExecutorOfDtoDependentGenerator;
import org.laoruga.dtogenerator.generators.executors.ExecutorOfGenerator;

import java.lang.reflect.Field;
import java.util.HashMap;
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

    private final Supplier<Object> dtoInstanceSupplier;
    @Getter(AccessLevel.PACKAGE)
    private final FieldGeneratorsProvider fieldGeneratorsProvider;
    @Getter(AccessLevel.PACKAGE)
    private final DtoGeneratorBuilder<T> builderInstance;
    @Getter(AccessLevel.PACKAGE)
    private final ErrorsHolder errorsHolder;
    private BatchGeneratorsExecutor batchGeneratorsExecutor;

    protected DtoGenerator(FieldGeneratorsProvider fieldGeneratorsProvider,
                           DtoGeneratorBuilder<T> dtoGeneratorBuilder) {
        this.fieldGeneratorsProvider = fieldGeneratorsProvider;
        this.builderInstance = dtoGeneratorBuilder;
        this.dtoInstanceSupplier = fieldGeneratorsProvider.getDtoInstanceSupplier();
        this.errorsHolder = new ErrorsHolder();
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
        try {

            synchronized (this) {
                if (batchGeneratorsExecutor == null) {
                    batchGeneratorsExecutor = new BatchGeneratorsExecutor(
                            new ExecutorOfDtoDependentGenerator(dtoInstanceSupplier,
                                    new ExecutorOfCollectionGenerator(dtoInstanceSupplier,
                                            new ExecutorOfGenerator(dtoInstanceSupplier))),
                            prepareGenerators(dtoInstanceSupplier.get().getClass(), new HashMap<>())
                    );
                }
            }

            batchGeneratorsExecutor.execute();


        } catch (Exception e) {
            throw new DtoGeneratorException(e);
        }
        return (T) dtoInstanceSupplier.get();
    }

    private Map<Field, IGenerator<?>> prepareGenerators(Class<?> dtoClass, Map<Field, IGenerator<?>> generatorMap) {

        if (dtoClass.getSuperclass() != null && dtoClass.getSuperclass() != Object.class) {
            prepareGenerators(dtoClass.getSuperclass(), generatorMap);
        }

        for (Field field : dtoClass.getDeclaredFields()) {
            Optional<IGenerator<?>> generator = Optional.empty();
            try {
                generator = getFieldGeneratorsProvider().getGenerator(field);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            }
            generator.ifPresent(typeGeneratorInstance ->
                    generatorMap.put(field, typeGeneratorInstance));
        }
        if (!errorsHolder.isEmpty()) {
            log.error("{} error(s) while generators preparation. See problems below: \n"
                    + errorsHolder, errorsHolder.getErrorsNumber());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (generatorMap.isEmpty()) {
            log.debug("Generators not found");
        } else {
            final AtomicInteger idx = new AtomicInteger(0);
            log.debug(generatorMap.size() + " generators created for fields: \n" +
                    generatorMap.keySet().stream()
                            .map(i -> idx.incrementAndGet() + ". " + i)
                            .collect(Collectors.joining("\n")));
        }

        return generatorMap;
    }

}
