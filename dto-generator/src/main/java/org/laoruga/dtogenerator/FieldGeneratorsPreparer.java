package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.NestedDtoGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 11.04.2023
 */
@Slf4j
@Getter(AccessLevel.PACKAGE)
public class FieldGeneratorsPreparer {

    private final FieldGenerators filedGenerators;
    private final ErrorsHolder errorsHolder;

    public FieldGeneratorsPreparer() {
        this.filedGenerators = new FieldGenerators();
        this.errorsHolder = new ErrorsHolder();
    }

    void prepareGenerators(Class<?> dtoClass,
                           FieldGeneratorsProvider fieldGeneratorsProvider) {

        prepareGenerators(dtoClass, fieldGeneratorsProvider, new HashMap<>());

        if (!errorsHolder.isEmpty()) {
            throw new DtoGeneratorException("'" + errorsHolder.getErrorsNumber() + "'" +
                    " error(s) during generators preparation:\n" + errorsHolder);
        }

        log.debug(filedGenerators.toString());
    }

    private void prepareGenerators(Class<?> dtoClass,
                                   FieldGeneratorsProvider fieldGeneratorsProvider,
                                   Map<Field, Generator<?>> generatorMap) {
        final Supplier<?> dtoInstanceSupplier =
                fieldGeneratorsProvider.getDtoInstanceSupplier();

        filedGenerators.addGenerator(dtoInstanceSupplier, generatorMap);

        if (dtoClass.getSuperclass() != null && dtoClass.getSuperclass() != Object.class) {
            prepareGenerators(dtoClass.getSuperclass(), fieldGeneratorsProvider, generatorMap);
        }

        for (Field field : dtoClass.getDeclaredFields()) {

            if (Modifier.isFinal(field.getModifiers())) {
                log.info("Skipping final field '" + field.getType() + " " + field.getName() + "'");
                continue;
            }

            Optional<Generator<?>> maybeGenerator = Optional.empty();

            try {
                maybeGenerator = fieldGeneratorsProvider.getGenerator(field);
            } catch (Exception e) {
                errorsHolder.put(field, e);
            }

            if (maybeGenerator.isPresent()) {

                Generator<?> generator = maybeGenerator.get();

                if (generator instanceof NestedDtoGenerator) {
                    prepareGeneratorsForNestedDto(field, (NestedDtoGenerator) generator, dtoInstanceSupplier);
                } else {
                    generatorMap.put(field, generator);
                }

            } else {

                log.debug("Generator not found for field: '" + field.getName() + "'");

            }

        }

    }

    private void prepareGeneratorsForNestedDto(Field field,
                                               NestedDtoGenerator nestedDtoGenerator,
                                               Supplier<?> dtoInstanceSupplier) {

        FieldGeneratorsProvider nestedFieldGeneratorsProvider =
                nestedDtoGenerator.getDtoGeneratorBuilderTreeNode().getFieldGeneratorsProvider();

        filedGenerators.addNestedGenerator(dtoInstanceSupplier, nestedDtoGenerator, field);

        prepareGenerators(
                field.getType(),
                nestedFieldGeneratorsProvider,
                new HashMap<>()
        );
    }

}
