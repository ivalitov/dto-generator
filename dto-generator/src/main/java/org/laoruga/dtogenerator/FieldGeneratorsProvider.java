package org.laoruga.dtogenerator;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.providers.GeneratorBuildersProvider;
import org.laoruga.dtogenerator.generators.providers.GeneratorBuildersProviderByAnnotation;
import org.laoruga.dtogenerator.generators.providers.GeneratorBuildersProviderByField;
import org.laoruga.dtogenerator.generators.providers.GeneratorBuildersProviderByType;
import org.laoruga.dtogenerator.rules.IRuleInfo;
import org.laoruga.dtogenerator.rules.RulesInfoExtractor;
import org.laoruga.dtogenerator.rules.RulesInfoHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.getDefaultMethodValue;

/**
 * @author Il'dar Valitov
 * Created on 15.05.2022
 */

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class FieldGeneratorsProvider {

    private final DtoGeneratorInstanceConfig configuration;
    private Supplier<Object> dtoInstanceSupplier;
    private final String[] pathFromDtoRoot;
    private final Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree;
    private final RemarksHolder remarksHolder;
    private final Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders;
    private final Map<String, IGeneratorBuilder> overriddenBuildersForFields;
    private final GeneratorBuildersHolder userGenBuildersMapping;
    @Getter
    private final RulesInfoExtractor rulesInfoExtractor;
    private final GeneratorBuildersProvider generatorBuildersProvider;

    FieldGeneratorsProvider(DtoGeneratorInstanceConfig configuration,
                            RemarksHolder typeGeneratorRemarksProvider,
                            FieldGroupFilter fieldGroupFilter,
                            String[] pathFromDtoRoot,
                            Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree) {
        this.configuration = configuration;
        this.overriddenBuildersForFields = new HashMap<>();
        this.userGenBuildersMapping = new GeneratorBuildersHolder();
        this.remarksHolder = typeGeneratorRemarksProvider;
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.overriddenBuilders = new ConcurrentHashMap<>();
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldGroupFilter);
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.generatorBuildersProvider = initGeneratorProviders(overriddenBuildersForFields);
    }

    /**
     * Constructor to copy
     *
     * @param copyFrom source object
     */
    FieldGeneratorsProvider(FieldGeneratorsProvider copyFrom, String[] pathFromDtoRoot) {
        this.configuration = copyFrom.getConfiguration();
        this.overriddenBuildersForFields = new HashMap<>();
        this.userGenBuildersMapping = copyFrom.getUserGenBuildersMapping();
        this.remarksHolder = new RemarksHolder(copyFrom.getRemarksHolder());
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.overriddenBuilders = copyFrom.getOverriddenBuilders();
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
        this.generatorBuildersProvider = initGeneratorProviders(overriddenBuildersForFields);
    }

    /**
     * Setter for delayed field initialisation.
     * When nested DTO generation params are filling in {@link DtoGeneratorBuilder},
     * we don't know type of nested field yet.
     *
     * @param dtoInstance dto instance to build
     */
    void setDtoInstanceSupplier(Supplier<Object> dtoInstance) {
        try {
            this.dtoInstanceSupplier = dtoInstance;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error", e);
        }
    }


    private GeneratorBuildersProvider initGeneratorProviders(Map<String, IGeneratorBuilder> buildersMapping) {
        GeneratorBuildersProviderByType generatorBuildersProviderByType = new GeneratorBuildersProviderByType(
                configuration,
                userGenBuildersMapping,
                remarksHolder);
        GeneratorBuildersProviderByField generatorBuildersProviderByField = new GeneratorBuildersProviderByField(
                configuration,
                buildersMapping,
                remarksHolder);
        GeneratorBuildersProviderByAnnotation generatorBuildersProviderByAnnotation = new GeneratorBuildersProviderByAnnotation(
                configuration,
                generatorBuildersProviderByType,
                remarksHolder,
                userGenBuildersMapping);
        return new GeneratorBuildersProvider(
                generatorBuildersProviderByField,
                generatorBuildersProviderByType,
                generatorBuildersProviderByAnnotation);
    }

    /**
     * Returns generator Instance for field value generation
     *
     * @param field - validated field
     * @return empty optional if:
     * - no rules annotations found
     * - rules annotations skipped by group
     * - no explicit generators attached for the field
     * else generator instance
     */
    Optional<IGenerator<?>> getGenerator(Field field) {

        if (generatorBuildersProvider.isGeneratorCreated(field)) {
            System.out.println("YES");
        }

        // generator was set explicitly
        if (getOverriddenBuildersForFields().containsKey(field.getName())) {
            return Optional.of(
                    generatorBuildersProvider.getGeneratorBuildersProviderOverriddenForField(field));
        }

        Optional<IRuleInfo> maybeRulesInfo = getRuleInfo(field);

        // field annotated with rules
        if (maybeRulesInfo.isPresent()) {
            return Optional.of(
                    generatorBuildersProvider.generatorBuildersProviderByAnnotation(
                            field,
                            maybeRulesInfo.get(),
                            getDtoInstanceSupplier(),
                            () -> {
                                String[] pathToNestedDtoField =
                                        Arrays.copyOf(pathFromDtoRoot, pathFromDtoRoot.length + 1);
                                pathToNestedDtoField[pathFromDtoRoot.length] = field.getName();
                                DtoGeneratorBuilderTreeNode nestedDtoGeneratorBuilder =
                                        dtoGeneratorBuildersTree.get().getBuilderLazy(pathToNestedDtoField);
                                nestedDtoGeneratorBuilder.getFieldGeneratorsProvider().setDtoInstanceSupplier(
                                        new DtoInstanceSupplier(field.getType()));
                                return nestedDtoGeneratorBuilder.build();
                            })
            );
        }

        // try to generate value by field type
        if (getConfiguration().getGenerateAllKnownTypes()) {
            return generatorBuildersProvider.getGeneratorBuildersProviderByType(field, field.getType());
        }

        return Optional.empty();
    }

    private Optional<IRuleInfo> getRuleInfo(Field field) {
        try {
            AnnotatingErrorsHandler.ResultDto validationResult =
                    new AnnotatingErrorsHandler(field.getDeclaredAnnotations(), getConfiguration()).validate();
            if (!validationResult.getResultString().isEmpty()) {
                throw new DtoGeneratorException("Field annotated wrong:\n" + validationResult.getResultString());
            }
            return rulesInfoExtractor.checkAndWrapAnnotations(field);
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while extracting rule annotations from field: '" + field + "'", e);
        }
    }

    void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder genBuilder) throws DtoGeneratorException {
        if (overriddenBuildersForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for field: '" + fieldName + "'");
        }
        overriddenBuildersForFields.put(fieldName, genBuilder);
    }

    void overrideGenerator(Class<? extends Annotation> rulesClass, @NonNull IGeneratorBuilder genBuilder) {
        try {
            Class<?> generatedType = getDefaultMethodValue(rulesClass, "generatedType", Class.class);
            getUserGenBuildersMapping().addBuilder(
                    rulesClass,
                    generatedType,
                    genBuilder);
        } catch (NoSuchMethodException e) {
            throw new DtoGeneratorException("Rules annotation '" + rulesClass.getName() +
                    "' does not contain 'generatedType' method with return type 'Class'", e);
        }

    }

    /**
     * @author Il'dar Valitov
     * Created on 11.11.2022
     */
    @RequiredArgsConstructor
    static class AnnotatingErrorsHandler {

        private final Annotation[] annotations;
        private final ResultDto resultDto = new ResultDto();
        private final DtoGeneratorInstanceConfig configuration;

        void count() {
            for (Annotation annotation : annotations) {

                if (RulesInfoHelper.isItRule(annotation)) {
                    resultDto.generalRule++;
                }

                if (RulesInfoHelper.isItMultipleRules(annotation)) {
                    resultDto.groupOfGeneralRules++;
                }

                if (RulesInfoHelper.isItCollectionRule(annotation)) {
                    resultDto.collectionRule++;
                }

                if (RulesInfoHelper.isItCollectionRules(annotation)) {
                    resultDto.groupOfCollectionRules++;
                }
            }
        }

        public ResultDto validate() {
            count();
            int idx = 0;

            if (resultDto.generalRule > 1) {
                resultDto.resultString
                        .append(++idx)
                        .append(". Found '")
                        .append(resultDto.generalRule)
                        .append("' @Rule annotations for various types, ")
                        .append("expected 1 or 0.")
                        .append("\n");
            }

            if (resultDto.groupOfGeneralRules > 1) {
                resultDto.resultString
                        .append(++idx)
                        .append(". Found '")
                        .append(resultDto.groupOfGeneralRules)
                        .append("' @Rules annotations for various types, expected @Rules for single type only.")
                        .append("\n");
            }

            if (resultDto.collectionRule > 1) {
                resultDto.resultString
                        .append(++idx)
                        .append(". Found '")
                        .append(resultDto.collectionRule)
                        .append("' @CollectionRule annotations for various collection types, expected 1 or 0.")
                        .append("\n");
            }

            if (resultDto.groupOfCollectionRules > 1) {
                resultDto.resultString
                        .append(++idx)
                        .append(". Found '")
                        .append(resultDto.groupOfCollectionRules)
                        .append("' @CollectionRules annotations for various collection types, ")
                        .append("expected @CollectionRules for single collection type only.")
                        .append("\n");
            }

            if (!configuration.getGenerateAllKnownTypes() &&
                    (resultDto.getSumOfCollectionRules() > 0) &&
                    (resultDto.getSumOfCollectionRules() != resultDto.getSumOfGeneralRules())) {
                resultDto.resultString
                        .append(++idx)
                        .append(". Missed @Rule annotation for item of collection.")
                        .append("\n");
            }

            return resultDto;
        }

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        static class ResultDto {

            private final StringBuilder resultString = new StringBuilder();
            private int generalRule = 0;
            private int groupOfGeneralRules = 0;
            private int collectionRule = 0;
            private int groupOfCollectionRules = 0;

            public String getResultString() {
                return resultString.toString();
            }

            int getSumOfCollectionRules() {
                return collectionRule + groupOfCollectionRules;
            }

            int getSumOfGeneralRules() {
                return generalRule + groupOfGeneralRules;
            }
        }

    }
}
