package org.laoruga.dtogenerator.generator.providers;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.generator.supplier.GeneratorSuppliers;
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
                                      RemarksHolder remarksHolder) {
        generatorsProviderByType = new GeneratorsProviderByType(
                configuration,
                userGenBuildersMapping,
                remarksHolder);
        generatorProviderOverriddenForField = new GeneratorsProviderByField(
                configuration,
                remarksHolder);
        generatorsProviderByAnnotation =
                new GeneratorsProviderByAnnotation(
                        configuration,
                        generatorsProviderByType,
                        remarksHolder,
                        userGenBuildersMapping);
        generatorsProviderByAnnotationForMap =
                new GeneratorsProviderByAnnotationForMap(generatorsProviderByAnnotation);
        generatorsProviderByAnnotationForCollection =
                new GeneratorsProviderByAnnotationForList(generatorsProviderByAnnotation);
        generatorsProviderByAnnotationForArray =
                new GeneratorsProviderByAnnotationForArray(generatorsProviderByAnnotation);
    }

    /*
     * By field
     */

    public synchronized boolean isGeneratorOverridden(String fieldName) {
        return generatorProviderOverriddenForField.isGeneratorOverridden(fieldName);
    }

    public synchronized void setGeneratorForField(String fieldName, IGenerator<?> generator) {
        generatorProviderOverriddenForField.setGeneratorBuilderForField(fieldName, generator);
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
                                                  Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {
        IGenerator<?> generator;

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {
            generator = generatorsProviderByAnnotationForCollection
                    .getGenerator((RuleInfoCollection) ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorSupplier);

        } else if (ruleInfo.isTypesEqual(RuleType.ARRAY)) {
            generator = generatorsProviderByAnnotationForArray
                    .getGenerator((RuleInfoCollection) ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorSupplier);

        } else if (ruleInfo.isTypesEqual(RuleType.MAP)) {
            generator = generatorsProviderByAnnotationForMap
                    .getGenerator((RuleInfoMap) ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorSupplier);

        } else {
            generator = generatorsProviderByAnnotation
                    .getGenerator(ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorSupplier);
        }

        generatorsProviderByAnnotation.prepareCustomRemarks(generator, ruleInfo.getField().getName());

        return generator;
    }
}
