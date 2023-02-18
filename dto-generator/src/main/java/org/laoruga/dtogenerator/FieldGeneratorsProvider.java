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
    private final Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders;
    private final GeneratorBuildersHolder userGenBuildersMapping;
    @Getter
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
        this.overriddenBuilders = new HashMap<>();
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldsFilter);
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.generatorProvidersMediator = new GeneratorProvidersMediator(configuration, userGenBuildersMapping, remarksProvider);
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }

    /**
     * Constructor to copy
     *
     * @param copyFrom      source object
     * @param remarksHolder
     */
    FieldGeneratorsProvider(FieldGeneratorsProvider copyFrom, RemarksHolder remarksHolder, String[] pathFromDtoRoot) {
        this.configuration = copyFrom.getConfiguration();
        this.userGenBuildersMapping = copyFrom.getUserGenBuildersMapping();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.overriddenBuilders = copyFrom.getOverriddenBuilders();
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
        if (generatorProvidersMediator.isBuilderOverridden(field.getName())) {
            return Optional.of(
                    generatorProvidersMediator.getGeneratorOverriddenForField(field));
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

        // try to generate value by field type
        if (getConfiguration().getGenerateAllKnownTypes()) {
            return generatorProvidersMediator.getGeneratorsByType(field, field.getType());
        }

        return Optional.empty();
    }

    void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder genBuilder) throws DtoGeneratorException {
        generatorProvidersMediator.setGeneratorBuilderForField(fieldName, genBuilder);
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
