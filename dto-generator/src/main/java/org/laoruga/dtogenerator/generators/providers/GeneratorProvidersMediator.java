package org.laoruga.dtogenerator.generators.providers;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.rules.IRuleInfo;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.02.2023
 */
public class GeneratorProvidersMediator {

    private final GeneratorsProviderByField generatorBuildersProviderOverriddenForField;
    private final GeneratorsProviderByType generatorsProviderByType;
    private final generatorsProviderByAnnotationSupportingCollections generatorsProviderByAnnotation;
    private final RemarksHolder remarksHolder;

    public GeneratorProvidersMediator(DtoGeneratorInstanceConfig configuration,
                                      GeneratorBuildersHolder userGenBuildersMapping,
                                      RemarksHolder remarksHolder) {
        generatorsProviderByType = new GeneratorsProviderByType(
                configuration,
                userGenBuildersMapping,
                remarksHolder);
        generatorBuildersProviderOverriddenForField = new GeneratorsProviderByField(
                configuration,
                remarksHolder);
        generatorsProviderByAnnotation =
                new generatorsProviderByAnnotationSupportingCollections(
                        configuration,
                        generatorsProviderByType,
                        remarksHolder,
                        userGenBuildersMapping);
        this.remarksHolder = remarksHolder;
    }

    public RemarksHolder getRemarksHolder() {
        return remarksHolder;
    }

    //    /**
//     * Constructor to copy
//     *
//     * @param generatorsProvider copy from
//     */
//    public GeneratorProvidersMediator(GeneratorProvidersMediator generatorsProvider) {
//        this(generatorsProvider.generatorBuildersProviderOverriddenForField,
//                generatorsProvider.generatorsProviderByType,
//                generatorsProvider.generatorsProviderByAnnotation);
//    }

    public boolean isBuilderOverridden(String fieldName) {
        return generatorBuildersProviderOverriddenForField.isBuilderOverridden(fieldName);
    }

    public void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder genBuilder) {
        generatorBuildersProviderOverriddenForField.setGeneratorBuilderForField(fieldName, genBuilder);
    }

    public IGenerator<?> getGeneratorOverriddenForField(Field field) {
        return generatorBuildersProviderOverriddenForField.getGenerator(field);
    }


    public Optional<IGenerator<?>> getGeneratorsByType(Field field, Class<?> generatedType) {
        return generatorsProviderByType
                .getGenerator(field, generatedType)
                .map(Objects::requireNonNull);
    }

    public IGenerator<?> getGeneratorByAnnotation(Field field,
                                                  IRuleInfo ruleInfo,
                                                  Supplier<?> dtoInstanceSupplier,
                                                  Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {
        IGenerator<?> generator;

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {
            generator = generatorsProviderByAnnotation
                    .getCollectionGenerator(field, ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorSupplier);
        } else {
            generator = generatorsProviderByAnnotation
                    .getGenerator(field, ruleInfo, dtoInstanceSupplier, nestedDtoGeneratorSupplier);
        }

        generatorsProviderByAnnotation.prepareCustomRemarks(generator, field.getName());

        return generator;
    }


}
