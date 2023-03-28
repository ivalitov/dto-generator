package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.supplier.GeneralGeneratorSuppliers;
import org.laoruga.dtogenerator.generator.supplier.GeneratorSuppliers;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByType extends GeneratorsProviderAbstract {

    private final GeneratorSuppliers userGeneratorSuppliers;
    private final GeneratorSuppliers generalGeneratorSuppliers = GeneralGeneratorSuppliers.getInstance();

    public GeneratorsProviderByType(ConfigurationHolder configuration,
                                    GeneratorSuppliers userGeneratorSuppliers,
                                    RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.userGeneratorSuppliers = userGeneratorSuppliers;
    }

    public Optional<IGenerator<?>> getGenerator(Field field, Class<?> generatedType) {

        generatedType = Primitives.wrap(generatedType);

        Optional<Function<ConfigDto, IGenerator<?>>> maybeGeneratorSupplier = getGeneratorSupplier(generatedType);

        if (!maybeGeneratorSupplier.isPresent()) {
            log.debug("Generator supplier not found for field type: " + generatedType);
            return Optional.empty();
        }

        ConfigDto generatorConfig = getGeneratorConfig(field, generatedType);

        return Optional.of(
                maybeGeneratorSupplier.get().apply(generatorConfig)
        );
    }

    private Optional<Function<ConfigDto, IGenerator<?>>> getGeneratorSupplier(Class<?> generatedType) {

        Optional<Function<ConfigDto, IGenerator<?>>> maybeBuilder =
                userGeneratorSuppliers.getGeneratorSupplier(generatedType);

        if (!maybeBuilder.isPresent()) {
            maybeBuilder = generalGeneratorSuppliers.getGeneratorSupplier(generatedType);
        }

        return maybeBuilder;
    }

    @SuppressWarnings("unchecked")
    private ConfigDto getGeneratorConfig(Field field,
                                                   Class<?> generatedType) {
        Consumer<ConfigDto> specificConfig;

        if (CollectionRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            Class<? extends Collection<?>> generatedTypeCollection = (Class<? extends Collection<?>>) generatedType;
            Class<? extends Collection<?>> concreteCollectionClass =
                    (Class<? extends Collection<?>>) ConcreteClasses.getConcreteCollectionClass(generatedTypeCollection);

            Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
            IGenerator<?> elementGenerator =
                    getGenerator(field, elementType).orElseThrow(
                            () -> new DtoGeneratorException(
                                    "Collection element generator not found, for type: " + "'" + elementType + "'"));

            specificConfig = getCollectionGeneratorSupplier(
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

            specificConfig = getMapGeneratorSupplier(
                    concreteMapClass,
                    keyGenerator,
                    valueGenerator
            );

        } else if (DateTimeRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            specificConfig = getTemporalGeneratorSupplier((Class<Temporal>) generatedType);

        } else if (generatedType.isEnum() || field.getType() == Enum.class) {

            specificConfig = enumGeneratorSpecificConfig(generatedType);

        } else if (GeneratedTypes.isAssignableFrom(ArrayRule.GENERATED_TYPES, generatedType)) {

            Class<?> elementType = ReflectionUtils.getArrayElementType(generatedType);
            IGenerator<?> elementGenerator =
                    getGenerator(field, elementType).orElseThrow(
                            () -> new DtoGeneratorException(
                                    "Array element generator not found, for type: " + "'" + elementType + "'"));

            specificConfig = getArrayGeneratorSupplier(
                    elementType,
                    elementGenerator
            );

        } else {

            specificConfig = EMPTY_SPECIFIC_CONFIG;

        }

       return mergeGeneratorConfigurations(
                TypeGeneratorsDefaultConfigSupplier.getDefaultConfigSupplier(
                        Primitives.wrap(generatedType)
                ),
                specificConfig,
                generatedType,
                field.getName());
    }

}
