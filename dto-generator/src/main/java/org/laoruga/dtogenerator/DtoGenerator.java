package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.executors.BatchExecutor;
import org.laoruga.dtogenerator.generator.executors.ExecutorOfCollectionGenerator;
import org.laoruga.dtogenerator.generator.executors.ExecutorOfDtoDependentGenerator;
import org.laoruga.dtogenerator.generator.executors.ExecutorOfGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * DtoGenerator generates random field values:
 * 1) in passed object {@link DtoGenerator#builder(Object)};
 * 2) or in new objects instantiated by passed class {@link DtoGenerator#builder(Class)}.
 * DtoGenerator is thread safe, so you can use single instance to generate new objects
 * from different threads simultaneously.
 * Generation rules are configuring via {@link DtoGeneratorBuilder}.
 * You can't change configuration after instantiating.
 *
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator<T> {

    private final FieldGeneratorsProvider fieldGeneratorsProvider;
    @Getter(AccessLevel.PACKAGE)
    private final ErrorsHolder errorsHolder;
    private BatchExecutor batchExecutor;

    DtoGenerator(FieldGeneratorsProvider fieldGeneratorsProvider) {
        this.fieldGeneratorsProvider = fieldGeneratorsProvider;
        this.errorsHolder = new ErrorsHolder();
    }

    public static <T> DtoGeneratorBuilder<T> builder(Class<T> dtoClass) {
        return new DtoGeneratorBuilder<>(dtoClass);
    }

    public static <T> DtoGeneratorBuilder<T> builder(T dtoInstance) {
        return new DtoGeneratorBuilder<>(dtoInstance);
    }

    /**
     * @return updated or new DTO instance containing generated data
     */
    @SuppressWarnings("unchecked")
    public T generateDto() {

        Supplier<?> dtoInstanceSupplier = fieldGeneratorsProvider.getDtoInstanceSupplier();

        if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
            ((DtoInstanceSupplier) dtoInstanceSupplier).updateInstance();
        }

        Object dtoInstance;

        try {

            dtoInstance = dtoInstanceSupplier.get();

            synchronized (this) {
                if (batchExecutor == null) {

                    ExecutorOfGenerator generalGeneratorExecutor =
                            new ExecutorOfGenerator(dtoInstanceSupplier);

                    ExecutorOfCollectionGenerator collectionGeneratorExecutor =
                            new ExecutorOfCollectionGenerator(dtoInstanceSupplier, generalGeneratorExecutor);

                    ExecutorOfDtoDependentGenerator dtoDependentGeneratorExecutor =
                            new ExecutorOfDtoDependentGenerator(dtoInstanceSupplier, collectionGeneratorExecutor);

                    batchExecutor = new BatchExecutor(
                            dtoDependentGeneratorExecutor,
                            prepareGenerators(dtoInstance.getClass(), new HashMap<>())
                    );
                }
            }

            batchExecutor.execute();

        } catch (Exception e) {
            throw new DtoGeneratorException(e);
        } finally {
            if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
                ((DtoInstanceSupplier) dtoInstanceSupplier).remove();
            }
        }

        return (T) dtoInstance;
    }

    private Map<Field, Generator<?>> prepareGenerators(Class<?> dtoClass, Map<Field, Generator<?>> generatorMap) {

        if (dtoClass.getSuperclass() != null && dtoClass.getSuperclass() != Object.class) {
            prepareGenerators(dtoClass.getSuperclass(), generatorMap);
        }

        for (Field field : dtoClass.getDeclaredFields()) {

            if (Modifier.isFinal(field.getModifiers())) {
                log.info("Skipping final field '" + field.getType() + " " + field.getName() + "'");
                continue;
            }

            Optional<Generator<?>> generator = Optional.empty();
            try {
                generator = fieldGeneratorsProvider.getGenerator(field);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            }
            generator.ifPresent(typeGeneratorInstance ->
                    generatorMap.put(field, typeGeneratorInstance));
        }

        if (!errorsHolder.isEmpty()) {
            log.error("{} error(s) while generators preparation. See problems below:\n"
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
