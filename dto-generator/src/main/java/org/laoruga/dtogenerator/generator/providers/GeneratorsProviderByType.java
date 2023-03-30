package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.Configuration;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.CustomGeneratorConfigurator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfigurator;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSupplierInfo;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliersDefault;
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

    private final Configuration configuration;
    private final GeneratorConfigurator generatorConfigurator;
    private final GeneratorSuppliers userGeneratorSuppliers;
    private final Supplier<?> dtoInstanceSupplier;
    private final GeneratorSuppliers defaultGeneratorSuppliers = GeneratorSuppliersDefault.getInstance();

    public GeneratorsProviderByType(Configuration configuration,
                                    GeneratorConfigurator generatorConfigurator,
                                    GeneratorSuppliers userGeneratorSuppliers,
                                    Supplier<?> dtoInstanceSupplier) {
        this.configuration = configuration;
        this.generatorConfigurator = generatorConfigurator;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }

    public Optional<IGenerator<?>> getGenerator(Field field, Class<?> generatedType) {

        generatedType = Primitives.wrap(generatedType);

        Optional<GeneratorSupplierInfo> maybeUserGeneratorSupplierInfo =
                userGeneratorSuppliers.getGeneratorSupplierInfo(generatedType);

        Optional<GeneratorSupplierInfo> maybeDefaultGeneratorSupplierInfo =
                defaultGeneratorSuppliers.getGeneratorSupplierInfo(generatedType);

        if (!maybeUserGeneratorSupplierInfo.isPresent() && !maybeDefaultGeneratorSupplierInfo.isPresent()) {
            log.debug("Generator supplier not found for field type: " + generatedType);
            return Optional.empty();
        }

        if (maybeUserGeneratorSupplierInfo.isPresent()) {
            log.debug("Generator config not found for field type: '" + generatedType + "'. Try to use generator as is.");

            GeneratorSupplierInfo generatorSupplierInfo = maybeUserGeneratorSupplierInfo.get();

            IGenerator<?> userGenerator = generatorSupplierInfo.getGeneratorSupplier().apply(null);

            if (userGenerator instanceof ICustomGenerator) {
                CustomGeneratorConfigurator.builder()
                        .args(generatorSupplierInfo.getCustomGeneratorArgs())
                        .dtoInstanceSupplier(dtoInstanceSupplier)
                        .build()
                        .configure((ICustomGenerator<?>) userGenerator);
            }

            return Optional.of(generatorSupplierInfo.getGeneratorSupplier().apply(null));
        }

        Optional<ConfigDto> generatorConfig = getGeneratorConfig(field, generatedType);

        return generatorConfig.map(configDto ->
                maybeDefaultGeneratorSupplierInfo.get()
                        .getGeneratorSupplier()
                        .apply(configDto)
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
            IGenerator<?> elementGenerator =
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
        Optional<IGenerator<?>> maybeGenerator = getGenerator(field, elementType);

        if (!maybeGenerator.isPresent()) {
            if (configuration.getDtoGeneratorConfig().getGenerateAllKnownTypes()) {
                return Optional.empty();
            }
            throw new DtoGeneratorException(
                    "Collection element generator not found, for type: " + "'" + elementType + "'");
        }

        IGenerator<?> elementGenerator = maybeGenerator.get();

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

        Optional<IGenerator<?>> maybeKeyGenerator = getGenerator(field, keyValueTypes[0]);
        Optional<IGenerator<?>> maybeValueGenerator = getGenerator(field, keyValueTypes[1]);

        if (!maybeKeyGenerator.isPresent() || !maybeValueGenerator.isPresent()) {
            if (configuration.getDtoGeneratorConfig().getGenerateAllKnownTypes()) {
                return Optional.empty();
            }
            throw new DtoGeneratorException("Map key or value generator not found," +
                    " for key type: '" + keyValueTypes[1] + "'" +
                    " and value type: " + "'" + keyValueTypes[1] + "'");
        }

        IGenerator<?> keyGenerator = maybeKeyGenerator.orElseThrow(
                () -> new DtoGeneratorException(
                        "Map key generator not found, for type: " + "'" + keyValueTypes[0] + "'"));

        IGenerator<?> valueGenerator = getGenerator(field, keyValueTypes[1]).orElseThrow(
                () -> new DtoGeneratorException(
                        "Map value generator not found, for type: " + "'" + keyValueTypes[1] + "'"));

        return Optional.of(
                getMapGeneratorSpecificConfig(concreteMapClass, keyGenerator, valueGenerator)
        );
    }

}
