package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.executors.BatchExecutor;
import org.laoruga.dtogenerator.generator.executors.ExecutorOfCollectionGenerator;
import org.laoruga.dtogenerator.generator.executors.ExecutorOfDtoDependentGenerator;
import org.laoruga.dtogenerator.generator.executors.ExecutorOfGenerator;

import java.util.function.Supplier;

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

    @Getter(AccessLevel.PACKAGE)
    private final ErrorsHolder errorsHolder;
    private BatchExecutor batchExecutor;
    private final Supplier<?> dtoInstanceSupplier;

    private final FieldGenerators fieldGenerators;

    public DtoGenerator(FieldGenerators fieldGenerators, Supplier<?> dtoInstanceSupplier) {
        this.errorsHolder = new ErrorsHolder();
        this.fieldGenerators = fieldGenerators;
        this.dtoInstanceSupplier = dtoInstanceSupplier;
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

        Object dtoInstance;

        try {

            if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
                ((DtoInstanceSupplier) dtoInstanceSupplier).updateInstance();
            }

            dtoInstance = dtoInstanceSupplier.get();

            synchronized (this) {
                if (batchExecutor == null) {

                    ExecutorOfGenerator generalGeneratorExecutor =
                            new ExecutorOfGenerator();

                    ExecutorOfCollectionGenerator collectionGeneratorExecutor =
                            new ExecutorOfCollectionGenerator(generalGeneratorExecutor);

                    ExecutorOfDtoDependentGenerator dtoDependentGeneratorExecutor =
                            new ExecutorOfDtoDependentGenerator(collectionGeneratorExecutor);

                    batchExecutor = new BatchExecutor(
                            dtoDependentGeneratorExecutor,
                            fieldGenerators
                    );
                }
            }

            batchExecutor.execute();

        } catch (Exception e) {

            throw new DtoGeneratorException("Error during generators execution", e);

        } finally {

            if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
                ((DtoInstanceSupplier) dtoInstanceSupplier).remove();
            }

        }

        return (T) dtoInstance;
    }

}
