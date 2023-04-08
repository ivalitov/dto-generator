package org.laoruga.dtogenerator.generator.providers;

import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorByAnnotation;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForArray;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForList;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForMap;
import org.laoruga.dtogenerator.generator.providers.suppliers.UserGeneratorSuppliers;
import org.laoruga.dtogenerator.rule.RuleInfo;
import org.laoruga.dtogenerator.rule.RuleInfoList;
import org.laoruga.dtogenerator.rule.RuleInfoMap;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

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
                                      UserGeneratorSuppliers userGeneratorSuppliers,
                                      RemarksHolder remarksHolder,
                                      Function<String, DtoGeneratorBuilder<?>> nestedDtoGeneratorBuilderSupplier) {

        GeneratorConfiguratorByAnnotation configuratorByAnnotation =
                new GeneratorConfiguratorByAnnotation(
                        configuration,
                        remarksHolder,
                        nestedDtoGeneratorBuilderSupplier);

        this.generatorProviderOverriddenForField = new GeneratorsProviderByField();

        this.generatorsProviderByType = new GeneratorsProviderByType(
                configuration,
                configuratorByAnnotation,
                userGeneratorSuppliers
        );

        this.generatorsProviderByAnnotation =
                new GeneratorsProviderByAnnotation(
                        configuratorByAnnotation,
                        generatorsProviderByType,
                        userGeneratorSuppliers
                );

        this.generatorsProviderByAnnotationForMap =
                new GeneratorsProviderByAnnotationForMap(
                        generatorsProviderByAnnotation,
                        new GeneratorConfiguratorForMap(configuration, remarksHolder),
                        userGeneratorSuppliers
                );

        this.generatorsProviderByAnnotationForCollection =
                new GeneratorsProviderByAnnotationForList(
                        generatorsProviderByAnnotation,
                        new GeneratorConfiguratorForList(configuration, remarksHolder),
                        userGeneratorSuppliers
                );

        this.generatorsProviderByAnnotationForArray =
                new GeneratorsProviderByAnnotationForList(
                        generatorsProviderByAnnotation,
                        new GeneratorConfiguratorForArray(configuration, remarksHolder),
                        userGeneratorSuppliers
                );
    }

    /*
     * By field
     */

    public synchronized void setGeneratorForField(String fieldName, Generator<?> generator) {
        generatorProviderOverriddenForField.setGeneratorForField(
                fieldName,
                generator
        );
    }

    public synchronized Optional<Generator<?>> getGeneratorOverriddenForField(Field field) {
        return Optional.ofNullable(generatorProviderOverriddenForField.getGenerator(field));
    }

    /*
     * By type
     */

    public Optional<Generator<?>> getGeneratorByType(Field field, Class<?> generatedType) {
        return generatorsProviderByType
                .getGenerator(field, generatedType);
    }

    public void setGeneratorByType(Class<?> field, Generator<?> generatedType) {
        generatorsProviderByType
                .setGenerator(field, generatedType);
    }

    /*
     * By rules annotation
     */

    public Generator<?> getGeneratorByAnnotation(RuleInfo ruleInfo) {
        Generator<?> generator;

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {

            generator = generatorsProviderByAnnotationForCollection.getGenerator((RuleInfoList) ruleInfo);

        } else if (ruleInfo.isTypesEqual(RuleType.ARRAY)) {

            generator = generatorsProviderByAnnotationForArray.getGenerator((RuleInfoList) ruleInfo);

        } else if (ruleInfo.isTypesEqual(RuleType.MAP)) {

            generator = generatorsProviderByAnnotationForMap.getGenerator((RuleInfoMap) ruleInfo);

        } else {

            generator = generatorsProviderByAnnotation.getGenerator(ruleInfo);

        }

        return generator;
    }
}
