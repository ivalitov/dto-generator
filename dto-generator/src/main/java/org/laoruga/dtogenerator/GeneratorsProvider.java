package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.builders.AbstractGeneratorBuildersProvider;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersProviderByAnnotation;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersProviderByField;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersProviderByType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;
import static org.laoruga.dtogenerator.util.ReflectionUtils.getDefaultMethodValue;

/**
 * @author Il'dar Valitov
 * Created on 15.05.2022
 */

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class GeneratorsProvider<T> {

    private final DtoGeneratorInstanceConfig configuration;
    private T dtoInstance;
    private final String[] fieldsFromRoot;
    private final DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree;
    private final GeneratorRemarksProvider generatorRemarksProvider;
    private final Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders;
    private final Map<String, IGeneratorBuilder> overriddenBuildersForFields;

    private final GeneratorBuildersHolder userGenBuildersMapping;

    private final AbstractGeneratorBuildersProvider builderSelectChainByAnnotation;
    private final AbstractGeneratorBuildersProvider builderSelectChainByType;
    @Getter
    private final RulesInfoExtractor rulesInfoExtractor;

    GeneratorsProvider(DtoGeneratorInstanceConfig configuration,
                       GeneratorRemarksProvider generatorRemarksProvider,
                       FieldGroupFilter fieldGroupFilter,
                       String[] fieldsFromRoot,
                       DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree) {
        this.configuration = configuration;
        this.overriddenBuildersForFields = new HashMap<>();
        this.userGenBuildersMapping = new GeneratorBuildersHolder(new ArrayList<>());
        this.generatorRemarksProvider = generatorRemarksProvider;
        this.fieldsFromRoot = fieldsFromRoot;
        this.overriddenBuilders = new ConcurrentHashMap<>();
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldGroupFilter);
        this.generatorBuildersTree = generatorBuildersTree;
        this.builderSelectChainByType = initAllKnownTypesChain(overriddenBuildersForFields);
        this.builderSelectChainByAnnotation = initAnnotationChain(overriddenBuildersForFields);
    }

    /**
     * Constructor to copy
     *
     * @param copyFrom source object
     */
    GeneratorsProvider(GeneratorsProvider<?> copyFrom, String[] fieldsFromRoot) {
        this.configuration = copyFrom.configuration;
        this.overriddenBuildersForFields = new HashMap<>();
        this.userGenBuildersMapping = copyFrom.getUserGenBuildersMapping();
        this.generatorRemarksProvider = copyFrom.getGeneratorRemarksProvider().copy();
        this.fieldsFromRoot = fieldsFromRoot;
        this.overriddenBuilders = copyFrom.getOverriddenBuilders();
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.generatorBuildersTree = copyFrom.getGeneratorBuildersTree();
        this.builderSelectChainByType = copyFrom.initAllKnownTypesChain(overriddenBuildersForFields);
        this.builderSelectChainByAnnotation = copyFrom.initAnnotationChain(overriddenBuildersForFields);
    }

    /**
     * Setter for field delayed field initialisation.
     * When nested DTO generation params are filling in {@link DtoGeneratorBuilder},
     * we don't know type of nested field yet.
     *
     * @param dtoInstance dto instance to build
     */
    void setDtoInstance(Object dtoInstance) {
        if (this.dtoInstance != null) {
            throw new DtoGeneratorException("Dto instance has already been set: '" + this.dtoInstance.getClass() + "'");
        }
        try {
            this.dtoInstance = (T) dtoInstance;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error", e);
        }
    }

    private AbstractGeneratorBuildersProvider initAllKnownTypesChain(
            Map<String, IGeneratorBuilder> buildersMapping) {
        GeneratorBuildersProviderByField byField = new GeneratorBuildersProviderByField(
                configuration,
                buildersMapping);

        GeneratorBuildersProviderByType byType = new GeneratorBuildersProviderByType(
                configuration,
                userGenBuildersMapping);

        byField.addNextProvider(byType);

        return byField;
    }

    // TODO ремарки - таже проблема с ремарками для внутренних дто
    private AbstractGeneratorBuildersProvider initAnnotationChain(Map<String, IGeneratorBuilder> buildersMapping) {
        GeneratorBuildersProviderByField byField = new GeneratorBuildersProviderByField(
                configuration,
                buildersMapping);

        GeneratorBuildersProviderByAnnotation byAnnotation = new GeneratorBuildersProviderByAnnotation(
                configuration,
                new GeneratorBuildersProviderByType(configuration, userGenBuildersMapping),
                generatorRemarksProvider,
                userGenBuildersMapping);

        byField.addNextProvider(byAnnotation);

        return byField;
    }

    @RequiredArgsConstructor
    public
    class ProvidersVisitor {
        final Field field;
        final IRuleInfo ruleInfo;

        public void visit(AbstractGeneratorBuildersProvider abstractProvider) {
            if (abstractProvider.getClass() == GeneratorBuildersProviderByType.class) {
                visitByType((GeneratorBuildersProviderByType) abstractProvider);
            } else if (abstractProvider.getClass() == GeneratorBuildersProviderByField.class) {
                visitByField((GeneratorBuildersProviderByField) abstractProvider);
            } else if (abstractProvider.getClass() == GeneratorBuildersProviderByAnnotation.class) {
                visitByAnnotation((GeneratorBuildersProviderByAnnotation) abstractProvider);
            } else {
                throw new DtoGeneratorException("Visitor no defined");
            }
        }

        void visitByType(GeneratorBuildersProviderByType byType) {
            byType.setField(field);
        }

        void visitByField(GeneratorBuildersProviderByField byField) {
            byField.setField(field);
        }

        void visitByAnnotation(GeneratorBuildersProviderByAnnotation byAnnotation) {
            byAnnotation.setField(field);
            byAnnotation.setMaybeRemark(
                    generatorRemarksProvider.isBasicRuleRemarkExists(field.getName()) ?
                            generatorRemarksProvider.getBasicRuleRemark(field.getName()) : null);
            byAnnotation.setDtoInstance(dtoInstance);
            byAnnotation.setRuleInfo(ruleInfo);
            byAnnotation.setNestedDtoGeneratorSupplier(() -> {
                        String[] pathToNestedDtoField = Arrays.copyOf(fieldsFromRoot, fieldsFromRoot.length + 1);
                        pathToNestedDtoField[fieldsFromRoot.length] = field.getName();
                        DtoGeneratorBuilder<?> nestedDtoGeneratorBuilder = generatorBuildersTree.getBuilder(pathToNestedDtoField);
                        nestedDtoGeneratorBuilder.getGeneratorsProvider().setDtoInstance(createInstance(field.getType()));
                        return nestedDtoGeneratorBuilder.build();
                    }
            );
        }
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
        Optional<IRuleInfo> maybeRulesInfo = getRuleInfo(field);

        // field annotated with rules
        if (maybeRulesInfo.isPresent()) {
            getBuilderSelectChainByAnnotation().accept(new ProvidersVisitor(field, maybeRulesInfo.get()));
            return getBuilderSelectChainByAnnotation().getGenerator();
        } else {
            getBuilderSelectChainByType().accept(new ProvidersVisitor(field, null));
            return getBuilderSelectChainByType().getGenerator();
        }

    }

    private Optional<IRuleInfo> getRuleInfo(Field field) {
        try {
            AnnotationErrorsHandler.ResultDto validationResult =
                    new AnnotationErrorsHandler(field.getDeclaredAnnotations(), configuration).validate();
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
}
