package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.providers.GeneratorProvidersMediator;
import org.laoruga.dtogenerator.generators.providers.GeneratorsProviderByField;
import org.laoruga.dtogenerator.generators.providers.GeneratorsProviderByType;
import org.laoruga.dtogenerator.generators.providers.generatorsProviderByAnnotationSupportingCollections;
import org.laoruga.dtogenerator.rules.IRuleInfo;
import org.laoruga.dtogenerator.rules.RulesInfoExtractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private Supplier<?> dtoInstanceSupplier;
    private final String[] pathFromDtoRoot;
    private final Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree;
    private final RemarksHolder remarksHolder;
    private final Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders;
    private final GeneratorBuildersHolder userGenBuildersMapping;
    @Getter
    private final RulesInfoExtractor rulesInfoExtractor;
    private final GeneratorProvidersMediator generatorsProvider;

    FieldGeneratorsProvider(DtoGeneratorInstanceConfig configuration,
                            RemarksHolder typeGeneratorRemarksProvider,
                            FieldFilter fieldsFilter,
                            String[] pathFromDtoRoot,
                            Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree,
                            Supplier<?> dtoInstanceSupplier) {
        this.configuration = configuration;
        this.userGenBuildersMapping = new GeneratorBuildersHolder();
        this.remarksHolder = typeGeneratorRemarksProvider;
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.overriddenBuilders = new HashMap<>();
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldsFilter);
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.generatorsProvider = initGeneratorProviders();
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }

    /**
     * Constructor to copy
     *
     * @param copyFrom source object
     */
    FieldGeneratorsProvider(FieldGeneratorsProvider copyFrom, String[] pathFromDtoRoot) {
        this.configuration = copyFrom.getConfiguration();
        this.userGenBuildersMapping = copyFrom.getUserGenBuildersMapping();
        this.remarksHolder = new RemarksHolder(copyFrom.getRemarksHolder());
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.overriddenBuilders = copyFrom.getOverriddenBuilders();
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
        this.generatorsProvider = initGeneratorProviders();
    }

    /**
     * Setter for delayed field initialisation.
     * When nested DTO params are setting {@link DtoGeneratorBuilder},
     * we don't know type of nested field yet.
     *
     * @param dtoInstanceSupplier dto instance to build
     */
    void setDtoInstanceSupplier(DtoInstanceSupplier dtoInstanceSupplier) {
        try {
            this.dtoInstanceSupplier = dtoInstanceSupplier;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error", e);
        }
    }


    private GeneratorProvidersMediator initGeneratorProviders() {
        GeneratorsProviderByType generatorsProviderByType = new GeneratorsProviderByType(
                configuration,
                userGenBuildersMapping,
                remarksHolder);
        GeneratorsProviderByField generatorsProviderByField = new GeneratorsProviderByField(
                configuration,
                remarksHolder);
        generatorsProviderByAnnotationSupportingCollections generatorBuildersProviderByAnnotation =
                new generatorsProviderByAnnotationSupportingCollections(
                        configuration,
                        generatorsProviderByType,
                        remarksHolder,
                        userGenBuildersMapping);
        return new GeneratorProvidersMediator(
                generatorsProviderByField,
                generatorsProviderByType,
                generatorBuildersProviderByAnnotation);
    }

    /**
     * Returns generator Instance for the field value generation
     *
     * @param field - validated field
     * @return empty optional if:
     * - no rules annotations found
     * - rules annotations skipped by group
     * - no explicit generators attached for the field
     * else generator instance
     */
    Optional<IGenerator<?>> getGenerator(Field field) {

        // generator was set explicitly
        if (generatorsProvider.isBuilderOverridden(field.getName())) {
            return Optional.of(
                    generatorsProvider.getGeneratorOverriddenForField(field));
        }

        Optional<IRuleInfo> maybeRulesInfo = getRuleInfo(field);

        // field annotated with rules
        if (maybeRulesInfo.isPresent()) {
            return Optional.of(
                    generatorsProvider.getGeneratorByAnnotation(
                            field,
                            maybeRulesInfo.get(),
                            getDtoInstanceSupplier(),
                            createDtoGeneratorSupplier(field))
            );
        }

        // try to generate value by field type
        if (getConfiguration().getGenerateAllKnownTypes()) {
            return generatorsProvider.getGeneratorsByType(field, field.getType());
        }

        return Optional.empty();
    }

    void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder genBuilder) throws DtoGeneratorException {
        generatorsProvider.setGeneratorBuilderForField(fieldName, genBuilder);
    }

    private Supplier<DtoGenerator<?>> createDtoGeneratorSupplier(Field field) {
        return () -> {
            String[] pathToNestedDtoField =
                    Arrays.copyOf(pathFromDtoRoot, pathFromDtoRoot.length + 1);
            pathToNestedDtoField[pathFromDtoRoot.length] = field.getName();
            DtoGeneratorBuilderTreeNode nestedDtoGeneratorBuilder =
                    dtoGeneratorBuildersTree.get().getBuilderLazy(pathToNestedDtoField);
            nestedDtoGeneratorBuilder.getFieldGeneratorsProvider().setDtoInstanceSupplier(
                    new DtoInstanceSupplier(field.getType())
            );
            return nestedDtoGeneratorBuilder.build();
        };
    }

    private Optional<IRuleInfo> getRuleInfo(Field field) {
        try {
            return rulesInfoExtractor.extractRulesInfo(field);
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while extracting rule annotations from field: '"
                    + field.getType() + " " + field.getName() + "'", e);
        }
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
