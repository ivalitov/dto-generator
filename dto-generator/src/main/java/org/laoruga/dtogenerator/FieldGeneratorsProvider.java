package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generator.providers.GeneratorProvidersMediator;
import org.laoruga.dtogenerator.rule.IRuleInfo;
import org.laoruga.dtogenerator.rule.RulesInfoExtractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
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
    private final GeneratorBuildersHolder userGenBuildersMapping;
    private final RulesInfoExtractor rulesInfoExtractor;
    private final GeneratorProvidersMediator generatorProvidersMediator;

    FieldGeneratorsProvider(DtoGeneratorInstanceConfig configuration,
                            RemarksHolder remarksProvider,
                            FieldFilter fieldsFilter,
                            String[] pathFromDtoRoot,
                            Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree,
                            Supplier<?> dtoInstanceSupplier) {
        this.configuration = configuration;
        this.userGenBuildersMapping = new GeneratorBuildersHolder();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldsFilter);
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.generatorProvidersMediator = new GeneratorProvidersMediator(
                configuration,
                userGenBuildersMapping,
                remarksProvider
        );
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }

    /**
     * Constructor to copy
     */
    FieldGeneratorsProvider(FieldGeneratorsProvider copyFrom, RemarksHolder remarksHolder, String[] pathFromDtoRoot) {
        this.configuration = copyFrom.getConfiguration();
        this.userGenBuildersMapping = copyFrom.getUserGenBuildersMapping();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
        this.generatorProvidersMediator = new GeneratorProvidersMediator(
                copyFrom.getConfiguration(),
                copyFrom.getUserGenBuildersMapping(),
                remarksHolder);
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

    /**
     * Returns generator instance for the field value generation.
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
        if (generatorProvidersMediator.isGeneratorBuilderOverridden(field.getName())) {
            return Optional.of(
                    generatorProvidersMediator.getGeneratorOverriddenForField(field)
            );
        }

        Optional<IRuleInfo> maybeRulesInfo = getRuleInfo(field);

        // field annotated with rules
        if (maybeRulesInfo.isPresent()) {
            return Optional.of(
                    generatorProvidersMediator.getGeneratorByAnnotation(
                            field,
                            maybeRulesInfo.get(),
                            getDtoInstanceSupplier(),
                            createDtoGeneratorSupplier(field))
            );
        }

        // attempt to generate value by field type
        if (getConfiguration().getGenerateAllKnownTypes()) {
            return generatorProvidersMediator.getGeneratorsByType(field, field.getType());
        }

        return Optional.empty();
    }

    void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder genBuilder) throws DtoGeneratorException {
        generatorProvidersMediator.setGeneratorBuilderForField(fieldName, genBuilder);
    }

    void overrideGenerator(Class<? extends Annotation> rulesClass, @NonNull IGeneratorBuilder genBuilder) {
        try {
            Class<?> generatedType = getDefaultMethodValue(rulesClass, "generatedType", Class.class);
            userGenBuildersMapping.addBuilder(
                    rulesClass,
                    generatedType,
                    genBuilder);
        } catch (NoSuchMethodException e) {
            throw new DtoGeneratorException("Rules annotation '" + rulesClass.getName() +
                    "' does not contain 'generatedType' method with return type 'Class'", e);
        }

    }

    void addGroups(String[] groups) {
        rulesInfoExtractor.getFieldsGroupFilter().includeGroups(groups);
    }

    private Supplier<DtoGenerator<?>> createDtoGeneratorSupplier(Field field) {
        return () -> {
            String[] pathToNestedDtoField =
                    Arrays.copyOf(pathFromDtoRoot, pathFromDtoRoot.length + 1);
            pathToNestedDtoField[pathFromDtoRoot.length] = field.getName();
            DtoGeneratorBuilder<?> nestedDtoGeneratorBuilder =
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
            throw new DtoGeneratorException("Error while extracting rule annotations from the field: '"
                    + field.getType() + " " + field.getName() + "'", e);
        }
    }
}
