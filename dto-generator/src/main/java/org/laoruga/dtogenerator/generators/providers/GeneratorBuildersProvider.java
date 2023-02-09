package org.laoruga.dtogenerator.generators.providers;

import lombok.RequiredArgsConstructor;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.rules.IRuleInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<Field, IGenerator<?>> generatorMap = new ConcurrentHashMap<>();

    public boolean isGeneratorCreated(Field field) {
        return generatorMap.containsKey(field);
    }

    public IGenerator<?> getGeneratorBuildersProviderOverriddenForField(Field field) {
        return generatorMap.compute(field, (key, oldValue) -> {
                    if (oldValue != null) {
//                        throw new IllegalStateException("Generator for field had already set: " + field);
                    }
                    return Objects.requireNonNull(generatorBuildersProviderOverriddenForField.getGenerator(field));
                }
        );
    }


    public Optional<IGenerator<?>> getGeneratorBuildersProviderByType(Field field, Class<?> generatedType) {
        return generatorBuildersProviderByType
                .getGenerator(field, generatedType)
                .map(generator ->
                        generatorMap.compute(field, (key, oldValue) -> {
                            if (oldValue != null) {
//                                throw new IllegalStateException("Generator for field had already set: " + field);
                            }
                            return Objects.requireNonNull(generator);
                        })
                );
    }

    public IGenerator<?> generatorBuildersProviderByAnnotation(Field field,
                                                               IRuleInfo ruleInfo,
                                                               Supplier<?> dtoInstanceSupplier,
                                                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {
        return generatorMap.compute(
                field,
                (key, oldValue) -> {
                    if (oldValue != null) {
//                        throw new IllegalStateException("Generator for field had already set: " + field);
                    }
                    return Objects.requireNonNull(generatorBuildersProviderByAnnotation.getGenerator(
                            field,
                            ruleInfo,
                            dtoInstanceSupplier,
                            nestedDtoGeneratorSupplier));
                });
    }
}
