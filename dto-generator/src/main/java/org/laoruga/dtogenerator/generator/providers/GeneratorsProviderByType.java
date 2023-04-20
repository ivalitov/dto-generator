package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.GeneratorConfigurator;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSupplierInfo;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliersDefault;
import org.laoruga.dtogenerator.generator.providers.suppliers.UserGeneratorSuppliers;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.generator.config.GeneratorConfigurator.*;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByType {

    private final ConfigurationHolder configuration;
    private final GeneratorConfigurator generatorConfigurator;
    private final UserGeneratorSuppliers userGeneratorSuppliers;
    private final GeneratorSuppliers defaultGeneratorSuppliers = GeneratorSuppliersDefault.getInstance();

    public GeneratorsProviderByType(ConfigurationHolder configuration,
                                    GeneratorConfigurator generatorConfigurator,
                                    UserGeneratorSuppliers userGeneratorSuppliers) {
        this.configuration = configuration;
        this.generatorConfigurator = generatorConfigurator;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
    }

    @SuppressWarnings("unchecked")
    public Optional<Generator<?>> getUserGenerator(Field field, Class<?> generatedType) {

        Optional<Generator<?>> maybeUserGenerator =
                userGeneratorSuppliers.getGenerator(generatedType);

        if (maybeUserGenerator.isPresent()) {
            Generator<?> generator = maybeUserGenerator.get();

            if (generator instanceof CustomGenerator) {
                configuration
                        .getCustomGeneratorsConfigurators()
                        .getBuilder(
                                field.getName(),
                                (Class<? extends CustomGenerator<?>>) generator.getClass()
                        )
                        .build()
                        .configure((CustomGenerator<?>) generator);
            }

        }

        return maybeUserGenerator;
    }


    @SuppressWarnings("unchecked")
    public Optional<Generator<?>> getGenerator(Field field, Class<?> generatedType) {

        generatedType = Primitives.wrap(generatedType);

        Optional<Generator<?>> maybeUserGenerator = getUserGenerator(field, generatedType);

        if (maybeUserGenerator.isPresent()) {

            return maybeUserGenerator;
        }

        Optional<GeneratorSupplierInfo> maybeDefaultGeneratorSupplier =
                defaultGeneratorSuppliers.getGeneratorSupplierInfo(generatedType);

        if (!maybeDefaultGeneratorSupplier.isPresent()) {
            log.debug("Generator supplier not found for field type: " + generatedType);
            return Optional.empty();
        }

        Optional<ConfigDto> generatorConfig = getGeneratorConfig(field, generatedType);

        if (!generatorConfig.isPresent()) {
            if (configuration.getDtoGeneratorConfig().getGenerateAllKnownTypes()) {
                return Optional.empty();
            }
            throw new DtoGeneratorException("Unexpected state.");
        }

        return Optional.of(
                maybeDefaultGeneratorSupplier.get()
                        .getGeneratorSupplier()
                        .apply(generatorConfig.get())
        );

    }

    @SuppressWarnings("unchecked")
    private Optional<ConfigDto> getGeneratorConfig(Field field,
                                                   Class<?> generatedType) {
        Consumer<ConfigDto> specificConfig;

        if (CollectionRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            Optional<Consumer<ConfigDto>> maybeCollectionSpecificConfig =
                    getCollectionSpecificConfigWithElementGenerator(field, generatedType);

            if (!maybeCollectionSpecificConfig.isPresent()) {
                return Optional.empty();
            }

            specificConfig = maybeCollectionSpecificConfig.get();

        } else if (MapRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            Optional<Consumer<ConfigDto>> maybeMapSpecificConfig =
                    getMapSpecificConfigWithKeyValueGenerators(field, generatedType);

            if (!maybeMapSpecificConfig.isPresent()) {
                return Optional.empty();
            }

            specificConfig = maybeMapSpecificConfig.get();

        } else if (DateTimeRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

            specificConfig = getTemporalGeneratorSpecificConfig((Class<Temporal>) generatedType);

        } else if (generatedType.isEnum() || field.getType() == Enum.class) {

            specificConfig = getEnumGeneratorSpecificConfig(generatedType);

        } else if (GeneratedTypes.isAssignableFrom(ArrayRule.GENERATED_TYPES, generatedType)) {

            Class<?> elementType = ReflectionUtils.getArrayElementType(generatedType);
            Generator<?> elementGenerator =
                    getGenerator(field, elementType).orElseThrow(
                            () -> new DtoGeneratorException(
                                    "Array element generator not found, for type: " + "'" + elementType + "'"));

            specificConfig = getArrayGeneratorSpecificConfig(
                    elementType,
                    elementGenerator
            );

        } else {

            specificConfig = EMPTY_SPECIFIC_CONFIG;

        }

        Optional<Supplier<ConfigDto>> maybeDefaultConfigSupplier =
                TypeGeneratorsDefaultConfigSupplier.getDefaultConfigSupplier(Primitives.wrap(generatedType));

        return maybeDefaultConfigSupplier.map(configDtoSupplier ->
                generatorConfigurator.mergeGeneratorConfigurations(
                        configDtoSupplier,
                        specificConfig,
                        generatedType,
                        field.getName()
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Optional<Consumer<ConfigDto>> getCollectionSpecificConfigWithElementGenerator(Field field, Class<?> generatedType) {
        Class<? extends Collection<?>> generatedTypeCollection = (Class<? extends Collection<?>>) generatedType;
        Class<? extends Collection<?>> concreteCollectionClass =
                (Class<? extends Collection<?>>) ConcreteClasses.getConcreteCollectionClass(generatedTypeCollection);

        Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
        Optional<Generator<?>> maybeGenerator = getGenerator(field, elementType);

        if (!maybeGenerator.isPresent()) {
            if (configuration.getDtoGeneratorConfig().getGenerateAllKnownTypes()) {
                return Optional.empty();
            }
            throw new DtoGeneratorException(
                    "Collection element generator not found, for type: " + "'" + elementType + "'");
        }

        Generator<?> elementGenerator = maybeGenerator.get();

        return Optional.of(
                getCollectionGeneratorSpecificConfig(concreteCollectionClass, elementGenerator)
        );
    }

    @SuppressWarnings("unchecked")
    private Optional<Consumer<ConfigDto>> getMapSpecificConfigWithKeyValueGenerators(Field field, Class<?> generatedType) {
        Class<? extends Map<?, ?>> generatedTypeMap = (Class<? extends Map<?, ?>>) generatedType;
        Class<? extends Map<?, ?>> concreteMapClass =
                (Class<? extends Map<?, ?>>) ConcreteClasses.getConcreteMapClass(generatedTypeMap);

        Class<?>[] keyValueTypes = ReflectionUtils.getPairedGenericType(field);

        Optional<Generator<?>> maybeKeyGenerator = getGenerator(field, keyValueTypes[0]);
        Optional<Generator<?>> maybeValueGenerator = getGenerator(field, keyValueTypes[1]);

        if (!maybeKeyGenerator.isPresent() || !maybeValueGenerator.isPresent()) {
            if (configuration.getDtoGeneratorConfig().getGenerateAllKnownTypes()) {
                return Optional.empty();
            }
            throw new DtoGeneratorException("Map key or value generator not found," +
                    " for key type: '" + keyValueTypes[1] + "'" +
                    " and value type: " + "'" + keyValueTypes[1] + "'");
        }

        Generator<?> keyGenerator = maybeKeyGenerator.orElseThrow(
                () -> new DtoGeneratorException(
                        "Map key generator not found, for type: " + "'" + keyValueTypes[0] + "'"));

        Generator<?> valueGenerator = getGenerator(field, keyValueTypes[1]).orElseThrow(
                () -> new DtoGeneratorException(
                        "Map value generator not found, for type: " + "'" + keyValueTypes[1] + "'"));

        return Optional.of(
                getMapGeneratorSpecificConfig(concreteMapClass, keyGenerator, valueGenerator)
        );
    }

    public void setGenerator(Class<?> generatedType, Generator<?> generator) {
        userGeneratorSuppliers.setGenerator(generatedType, () -> generator);
    }
}
