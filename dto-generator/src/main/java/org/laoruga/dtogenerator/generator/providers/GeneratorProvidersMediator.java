package org.laoruga.dtogenerator.generator.providers;

import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorByAnnotation;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForArray;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForList;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForMap;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.rule.IRuleInfo;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;
import org.laoruga.dtogenerator.rule.RuleInfoMap;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.02.2023
 */
public class GeneratorProvidersMediator {

    private final GeneratorsProviderByField generatorProviderOverriddenForField;
    private final GeneratorsProviderByType generatorsProviderByType;
    private final GeneratorsProviderByAnnotation generatorsProviderByAnnotation;
    private final GeneratorsProviderByAnnotationForMap generatorsProviderByAnnotationForMap;
    private final GeneratorsProviderByAnnotationForList generatorsProviderByAnnotationForCollection;
    private final GeneratorsProviderByAnnotationForList generatorsProviderByAnnotationForArray;

    public GeneratorProvidersMediator(ConfigurationHolder configuration,
                                      GeneratorSuppliers userGenBuildersMapping,
                                      RemarksHolder remarksHolder,
                                      Supplier<?> dtoInstanceSupplier) {
        GeneratorConfiguratorByAnnotation configuratorByAnnotation =
                new GeneratorConfiguratorByAnnotation(configuration, remarksHolder);

        this.generatorProviderOverriddenForField = new GeneratorsProviderByField(
                dtoInstanceSupplier
        );

        this.generatorsProviderByType = new GeneratorsProviderByType(
                configuration,
                configuratorByAnnotation,
                userGenBuildersMapping,
                dtoInstanceSupplier
        );

        this.generatorsProviderByAnnotation =
                new GeneratorsProviderByAnnotation(
                        configuratorByAnnotation,
                        generatorsProviderByType,
                        userGenBuildersMapping
                );

        this.generatorsProviderByAnnotationForMap =
                new GeneratorsProviderByAnnotationForMap(
                        generatorsProviderByAnnotation,
                        new GeneratorConfiguratorForMap(configuration, remarksHolder)
                );

        this.generatorsProviderByAnnotationForCollection =
                new GeneratorsProviderByAnnotationForList(
                        generatorsProviderByAnnotation,
                        new GeneratorConfiguratorForList(configuration, remarksHolder)
                );

        this.generatorsProviderByAnnotationForArray =
                new GeneratorsProviderByAnnotationForList(
                        generatorsProviderByAnnotation,
                        new GeneratorConfiguratorForArray(configuration, remarksHolder)
                );
    }

    /*
     * By field
     */

    public synchronized boolean isGeneratorOverridden(String fieldName) {
        return generatorProviderOverriddenForField.isGeneratorOverridden(fieldName);
    }

    public synchronized void setGeneratorForField(String fieldName, IGenerator<?> generator, String... args) {
        generatorProviderOverriddenForField.setGeneratorBuilderForField(fieldName, generator, args);
    }

    public synchronized IGenerator<?> getGeneratorOverriddenForField(Field field) {
        return generatorProviderOverriddenForField.getGenerator(field);
    }

    /*
     * By type
     */

    public Optional<IGenerator<?>> getGeneratorByType(Field field, Class<?> generatedType) {
        return generatorsProviderByType
                .getGenerator(field, generatedType)
                .map(Objects::requireNonNull);
    }

    /*
     * By rules annotation
     */

    public IGenerator<?> getGeneratorByAnnotation(IRuleInfo ruleInfo,
                                                  Supplier<?> dtoInstanceSupplier,
                                                  Supplier<DtoGeneratorBuilder<?>> nestedDtoGeneratorBuilderSupplier) {
        IGenerator<?> generator;

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {
            generator = generatorsProviderByAnnotationForCollection
                    .getGenerator((RuleInfoCollection) ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorBuilderSupplier);

        } else if (ruleInfo.isTypesEqual(RuleType.ARRAY)) {
            generator = generatorsProviderByAnnotationForArray
                    .getGenerator((RuleInfoCollection) ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorBuilderSupplier);

        } else if (ruleInfo.isTypesEqual(RuleType.MAP)) {
            generator = generatorsProviderByAnnotationForMap
                    .getGenerator((RuleInfoMap) ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorBuilderSupplier);

        } else {
            generator = generatorsProviderByAnnotation
                    .getGenerator(ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorBuilderSupplier);
        }

        generatorsProviderByAnnotation.prepareCustomRemarks(generator, ruleInfo.getField().getName());

        return generator;
    }
}
