package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.GeneratorProvidersMediator;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.rule.IRuleInfo;
import org.laoruga.dtogenerator.rule.RulesInfoExtractor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 15.05.2022
 */

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class FieldGeneratorsProvider {

    private final ConfigurationHolder configuration;
    private final Supplier<?> dtoInstanceSupplier;
    private final String[] pathFromDtoRoot;
    private final Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree;
    private final GeneratorSuppliers userGenBuildersMapping;
    private final RulesInfoExtractor rulesInfoExtractor;
    private final GeneratorProvidersMediator generatorProvidersMediator;

    FieldGeneratorsProvider(ConfigurationHolder configuration,
                            RemarksHolder remarksProvider,
                            FieldFilter fieldsFilter,
                            String[] pathFromDtoRoot,
                            Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree,
                            Supplier<?> dtoInstanceSupplier) {
        this.configuration = configuration;
        this.userGenBuildersMapping = new GeneratorSuppliers();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldsFilter);
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.generatorProvidersMediator = new GeneratorProvidersMediator(
                configuration,
                userGenBuildersMapping,
                remarksProvider,
                dtoInstanceSupplier,
                nestedDtoGeneratorBuilderSupplier());
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }

    /**
     * Constructor to copy
     */
    FieldGeneratorsProvider(FieldGeneratorsProvider copyFrom,
                            RemarksHolder remarksHolder,
                            String[] pathFromDtoRoot,
                            Supplier<?> dtoInstanceSupplier) {
        this.configuration = copyFrom.getConfiguration();
        this.userGenBuildersMapping = copyFrom.getUserGenBuildersMapping();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
        this.generatorProvidersMediator = new GeneratorProvidersMediator(
                copyFrom.getConfiguration(),
                copyFrom.getUserGenBuildersMapping(),
                remarksHolder,
                dtoInstanceSupplier,
                nestedDtoGeneratorBuilderSupplier());
        this.dtoInstanceSupplier = dtoInstanceSupplier;
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
        if (generatorProvidersMediator.isGeneratorOverridden(field.getName())) {
            return Optional.of(
                    generatorProvidersMediator.getGeneratorOverriddenForField(field)
            );
        }

        Optional<IRuleInfo> maybeRulesInfo = getRuleInfo(field);

        // field annotated with rules
        if (maybeRulesInfo.isPresent()) {
            return Optional.of(
                    generatorProvidersMediator.getGeneratorByAnnotation(maybeRulesInfo.get())
            );
        }

        // attempt to generate value by field type
        if (getConfiguration().getDtoGeneratorConfig().getGenerateAllKnownTypes()) {
            return generatorProvidersMediator.getGeneratorByType(field, field.getType());
        }

        return Optional.empty();
    }

    void setGeneratorBuilderForField(String fieldName, IGenerator<?> generator, String... args) throws DtoGeneratorException {
        generatorProvidersMediator.setGeneratorForField(fieldName, generator, args);
    }

    void setGenerator(Class<?> generatedType, @NonNull IGenerator<?> generator, String[] args) {
        userGenBuildersMapping.addSuppliersInfo(generatedType, generator, args);
    }

    void addGroups(String[] groups) {
        rulesInfoExtractor.getFieldsGroupFilter().includeGroups(groups);
    }

    private Function<String, DtoGeneratorBuilder<?>> nestedDtoGeneratorBuilderSupplier() {
        return (fieldName) -> {
            String[] pathToNestedDtoField =
                    Arrays.copyOf(pathFromDtoRoot, pathFromDtoRoot.length + 1);

            pathToNestedDtoField[pathFromDtoRoot.length] = fieldName;

            return dtoGeneratorBuildersTree.get().getBuilderLazy(pathToNestedDtoField);
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

    public void setGeneratorConfigForField(String fieldName, ConfigDto generatorConfig) {
        configuration.getTypeGeneratorsConfigForField()
                .setGeneratorConfigForField(fieldName, generatorConfig);
    }

    public void setGeneratorConfigForType(Class<?> generatedType, ConfigDto generatorConfig) {
        configuration.getTypeGeneratorsConfig()
                .setGeneratorConfigForType(generatedType, generatorConfig);
    }
}
