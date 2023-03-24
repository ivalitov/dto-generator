package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.generator.builder.builders.EnumGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByType extends GeneratorsProviderAbstract {

    private final GeneratorBuildersHolder userGeneratorBuilders;
    private final GeneratorBuildersHolder generalGeneratorBuilders = GeneratorBuildersHolderGeneral.getInstance();

    public GeneratorsProviderByType(ConfigurationHolder configuration,
                                    GeneratorBuildersHolder userGeneratorBuilders,
                                    RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.userGeneratorBuilders = userGeneratorBuilders;
    }

    public Optional<IGenerator<?>> getGenerator(Field field, Class<?> generatedType) {

        generatedType = Primitives.wrap(generatedType);

        Optional<IGeneratorBuilder<?>> maybeBuilder = getGeneratorBuilder(generatedType);

        if (!maybeBuilder.isPresent()) {
            log.debug("Generator builder not found for field type: " + generatedType);
            return Optional.empty();
        }

        IGeneratorBuilder<?> generatorBuilder = maybeBuilder.get();

        if (generatorBuilder instanceof IGeneratorBuilderConfigurable) {
            return configureGenerator(
                    field,
                    generatedType,
                    (IGeneratorBuilderConfigurable<?>) generatorBuilder);
        }

        log.debug("Unknown generator builder found by field type, trying to build 'as is' without configuring.");
        return Optional.of(generatorBuilder.build());
    }

    private Optional<IGeneratorBuilder<?>> getGeneratorBuilder(Class<?> generatedType) {
        Optional<IGeneratorBuilder<?>> maybeBuilder = userGeneratorBuilders.getBuilder(generatedType);
        if (!maybeBuilder.isPresent()) {
            maybeBuilder = generalGeneratorBuilders.getBuilder(generatedType);
        }
        return maybeBuilder;
    }


    @SuppressWarnings("unchecked")
    private Optional<IGenerator<?>> configureGenerator(Field field,
                                                       Class<?> generatedType,
                                                       IGeneratorBuilderConfigurable<?> genBuilder) {
        BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> generatorSupplier;

        if (CollectionRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            Class<? extends Collection<?>> generatedTypeCollection = (Class<? extends Collection<?>>) generatedType;
            Class<? extends Collection<?>> concreteCollectionClass =
                    (Class<? extends Collection<?>>) ConcreteClasses.getConcreteCollectionClass(generatedTypeCollection);

            Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
            IGenerator<?> elementGenerator =
                    getGenerator(field, elementType).orElseThrow(
                            () -> new DtoGeneratorException(
                                    "Collection element generator not found, for type: " + "'" + elementType + "'"));

            generatorSupplier = getCollectionGeneratorSupplier(
                    concreteCollectionClass,
                    elementGenerator
            );

        } else if (MapRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            Class<? extends Map<?, ?>> generatedTypeMap = (Class<? extends Map<?, ?>>) generatedType;
            Class<? extends Map<?, ?>> concreteMapClass =
                    (Class<? extends Map<?, ?>>) ConcreteClasses.getConcreteMapClass(generatedTypeMap);

            Class<?>[] keyValueTypes = ReflectionUtils.getPairedGenericType(field);

            IGenerator<?> keyGenerator = getGenerator(field, keyValueTypes[0]).orElseThrow(
                    () -> new DtoGeneratorException(
                            "Map key generator not found, for type: " + "'" + keyValueTypes[0] + "'"));

            IGenerator<?> valueGenerator = getGenerator(field, keyValueTypes[1]).orElseThrow(
                    () -> new DtoGeneratorException(
                            "Map value generator not found, for type: " + "'" + keyValueTypes[1] + "'"));

            generatorSupplier = getMapGeneratorSupplier(
                    concreteMapClass,
                    keyGenerator,
                    valueGenerator
            );

        } else if (DateTimeRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            generatorSupplier = getTemporalGeneratorSupplier((Class<Temporal>) generatedType);

        } else if (genBuilder instanceof EnumGeneratorBuilder) {

            generatorSupplier = getEnumGeneratorSupplier(generatedType);

        } else if (GeneratedTypes.isAssignableFrom(ArrayRule.GENERATED_TYPES, generatedType)) {

            Class<?> elementType = ReflectionUtils.getArrayElementType(generatedType);
            IGenerator<?> elementGenerator =
                    getGenerator(field, elementType).orElseThrow(
                            () -> new DtoGeneratorException(
                                    "Array element generator not found, for type: " + "'" + elementType + "'"));

            generatorSupplier = getArrayGeneratorSupplier(
                    elementType,
                    elementGenerator
            );

        } else {

            generatorSupplier = (config, builder) -> builder.build(config, true);

        }

        return Optional.of(
                getGenerator(
                        TypeGeneratorsDefaultConfigSupplier.getDefaultConfigSupplier(
                                Primitives.wrap(generatedType)
                        ),
                        () -> genBuilder,
                        generatorSupplier,
                        generatedType,
                        field.getName()));
    }

}
