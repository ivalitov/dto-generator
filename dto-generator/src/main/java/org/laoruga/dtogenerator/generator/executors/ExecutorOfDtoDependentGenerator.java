package org.laoruga.dtogenerator.generator.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.ArrayGenerator;
import org.laoruga.dtogenerator.generator.CollectionGenerator;
import org.laoruga.dtogenerator.generator.CustomGeneratorWrapper;
import org.laoruga.dtogenerator.generator.MapGenerator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class ExecutorOfDtoDependentGenerator extends ExecutorOfGenerator {

    public ExecutorOfDtoDependentGenerator(AbstractExecutor nextGenerator) {
        super(nextGenerator);
    }

    @Override
    public boolean execute(Field field, Generator<?> generator, Supplier<?> dtoInstanceSupplier) {
        if (isItDtoDependentGenerator(generator)) {
            if (isDtoReadyForFieldGeneration(generator)) {
                return super.execute(field, generator, dtoInstanceSupplier);
            }
            return false;
        }
        return executeNextInstead(field, generator, dtoInstanceSupplier);
    }

    /**
     * Check whether DTO is ready for using CustomGeneratorDtoDependent or not.
     * There is limited attempts to prevent infinite loops.
     *
     * @param generator - generator to check
     * @return - doesn't DTO ready?
     * @throws DtoGeneratorException - throws if all attempts are spent
     */
    boolean isDtoReadyForFieldGeneration(Generator<?> generator) throws DtoGeneratorException {

        CustomGeneratorDtoDependent<?, ?>[] dtoDependentGenerators = getDtoDependentGeneratorsOrNull(generator);

        boolean dtoReady = Arrays.stream(dtoDependentGenerators)
                .allMatch(CustomGeneratorDtoDependent::isDtoReady);

        log.debug("Object " + (dtoReady ? "is" : "isn't") + " ready to generate dependent field value");

        return dtoReady;
    }

    private static final CustomGeneratorDtoDependent<?, ?>[] EMPTY_ARRAY = {};

    CustomGeneratorDtoDependent<?, ?>[] getDtoDependentGeneratorsOrNull(Generator<?> generator) {
        if (generator instanceof CustomGeneratorDtoDependent) {

            return new CustomGeneratorDtoDependent[]{(CustomGeneratorDtoDependent<?, ?>) generator};

        } else if (generator instanceof ArrayGenerator) {

            return getDtoDependentGeneratorsOrNull(((ArrayGenerator) generator).getElementGenerator());

        } else if (generator instanceof CollectionGenerator) {

            return getDtoDependentGeneratorsOrNull(((CollectionGenerator) generator).getElementGenerator());

        } else if (generator instanceof MapGenerator) {

            CustomGeneratorDtoDependent<?, ?>[] keyGenerator =
                    getDtoDependentGeneratorsOrNull(((MapGenerator) generator).getKeyGenerator());

            CustomGeneratorDtoDependent<?, ?>[] valueGenerator =
                    getDtoDependentGeneratorsOrNull(((MapGenerator) generator).getValueGenerator());

            if (keyGenerator == EMPTY_ARRAY && valueGenerator == EMPTY_ARRAY) {
                return EMPTY_ARRAY;
            } else if (keyGenerator == EMPTY_ARRAY) {
                return valueGenerator;
            } else if (valueGenerator == EMPTY_ARRAY) {
                return keyGenerator;
            } else {
                CustomGeneratorDtoDependent<?, ?>[] result =
                        new CustomGeneratorDtoDependent<?, ?>[keyGenerator.length + valueGenerator.length];

                System.arraycopy(keyGenerator, 0, result, 0, keyGenerator.length);
                System.arraycopy(keyGenerator, 0, result, keyGenerator.length, valueGenerator.length);

                return result;
            }

        }
        if (generator instanceof CustomGeneratorWrapper) {

            return getDtoDependentGeneratorsOrNull(((CustomGeneratorWrapper) generator).getUsersGeneratorInstance());

        }

        return EMPTY_ARRAY;
    }


    boolean isItDtoDependentGenerator(Generator<?> generator) {
        return getDtoDependentGeneratorsOrNull(generator) != EMPTY_ARRAY;
    }
}
