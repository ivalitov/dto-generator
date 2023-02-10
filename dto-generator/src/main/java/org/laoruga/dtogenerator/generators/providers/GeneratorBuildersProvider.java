package org.laoruga.dtogenerator.generators.providers;

import lombok.RequiredArgsConstructor;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.rules.IRuleInfo;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.02.2023
 */
@RequiredArgsConstructor
public class GeneratorBuildersProvider {

    private final GeneratorBuildersProviderByField generatorBuildersProviderOverriddenForField;
    private final GeneratorBuildersProviderByType generatorBuildersProviderByType;
    private final GeneratorBuildersProviderByAnnotation generatorBuildersProviderByAnnotation;

    public IGenerator<?> getGeneratorBuildersProviderOverriddenForField(Field field) {
        return generatorBuildersProviderOverriddenForField.getGenerator(field);
    }

    public Optional<IGenerator<?>> getGeneratorBuildersProviderByType(Field field, Class<?> generatedType) {
        return generatorBuildersProviderByType
                .getGenerator(field, generatedType)
                .map(Objects::requireNonNull);
    }

    public IGenerator<?> generatorBuildersProviderByAnnotation(Field field,
                                                               IRuleInfo ruleInfo,
                                                               Supplier<?> dtoInstanceSupplier,
                                                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {
        return generatorBuildersProviderByAnnotation.getGenerator(
                field,
                ruleInfo,
                dtoInstanceSupplier,
                nestedDtoGeneratorSupplier);
    }
}
