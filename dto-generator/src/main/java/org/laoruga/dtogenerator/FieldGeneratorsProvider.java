package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.GeneratorProvidersMediator;
import org.laoruga.dtogenerator.generator.providers.suppliers.UserGeneratorSuppliers;
import org.laoruga.dtogenerator.rule.RuleInfo;
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
    @Getter(AccessLevel.PUBLIC)
    private final Supplier<?> dtoInstanceSupplier;
    private final String[] pathFromDtoRoot;
    private final Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree;
    private final UserGeneratorSuppliers userGeneratorSuppliers;
    private final RulesInfoExtractor rulesInfoExtractor;
    private final GeneratorProvidersMediator generatorProvidersMediator;
    private final RemarksHolder remarksHolder;
    private final CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder;

    FieldGeneratorsProvider(ConfigurationHolder configuration,
                            RemarksHolder remarksHolder,
                            CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder,
                            FieldFilter fieldsFilter,
                            String[] pathFromDtoRoot,
                            Supplier<DtoGeneratorBuildersTree> dtoGeneratorBuildersTree,
                            Supplier<?> dtoInstanceSupplier) {
        this.configuration = configuration;
        this.userGeneratorSuppliers = new UserGeneratorSuppliers();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldsFilter);
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.generatorProvidersMediator = new GeneratorProvidersMediator(
                configuration,
                userGeneratorSuppliers,
                remarksHolder,
                nestedDtoGeneratorBuilderSupplier()
        );
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        this.remarksHolder = remarksHolder;
        this.customGeneratorsConfigMapHolder = customGeneratorsConfigMapHolder;
    }

    /**
     * Constructor to copy for generation of nested DTO
     */
    FieldGeneratorsProvider(FieldGeneratorsProvider copyFrom,
                            RemarksHolder remarksHolder,
                            CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder,
                            String[] pathFromDtoRoot,
                            Supplier<?> dtoInstanceSupplier,
                            ConfigurationHolder configurationCopy) {
        this.configuration = configurationCopy;
        this.userGeneratorSuppliers = copyFrom.getUserGeneratorSuppliers();
        this.pathFromDtoRoot = pathFromDtoRoot;
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        this.generatorProvidersMediator = new GeneratorProvidersMediator(
                configurationCopy,
                userGeneratorSuppliers,
                remarksHolder,
                nestedDtoGeneratorBuilderSupplier()
        );
        this.remarksHolder = remarksHolder;
        this.customGeneratorsConfigMapHolder = customGeneratorsConfigMapHolder;
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
    @SuppressWarnings("unchecked")
    Optional<Generator<?>> getGenerator(Field field) {

        Optional<Generator<?>> maybeUserGeneratorForField =
                generatorProvidersMediator.getGeneratorOverriddenForField(field);

        // if generator set explicitly for field
        if (maybeUserGeneratorForField.isPresent()) {

            Generator<?> generatorForField = maybeUserGeneratorForField.get();

            if (generatorForField instanceof CustomGenerator) {
                configuration
                        .getCustomGeneratorsConfigurators()
                        .getBuilder(
                                field.getName(),
                                (Class<? extends CustomGenerator<?>>) generatorForField.getClass()
                        )
                        .build()
                        .configure((CustomGenerator<?>) generatorForField);
            }

            return Optional.of(generatorForField);
        }

        Optional<RuleInfo> maybeRulesInfo = getRuleInfo(field);

        // if field annotated with rules
        if (maybeRulesInfo.isPresent()) {
            return Optional.of(
                    generatorProvidersMediator.getGeneratorByAnnotation(maybeRulesInfo.get())
            );
        }

        DtoGeneratorConfig generatorConfig = configuration.getDtoGeneratorConfig();

        // if there needs to generate any known type
        // attempt to generate value using field type
        if (generatorConfig.getGenerateAllKnownTypes()) {
            return generatorProvidersMediator.getGeneratorByType(field, field.getType());
        }

        // if there needs to generate user's type only
        if (generatorConfig.getGenerateUsersTypes()) {

            Optional<Generator<?>> maybeUserGeneratorForType =
                    generatorProvidersMediator.getUserGeneratorByType(field, field.getType());

            if (maybeUserGeneratorForType.isPresent()) {
                return maybeUserGeneratorForType;
            }

        }

        return Optional.empty();
    }

    void setGeneratorBuilderForField(String fieldName, Generator<?> generator) throws DtoGeneratorException {
        generatorProvidersMediator.setGeneratorForField(fieldName, generator);
    }

    void setGenerator(Class<?> generatedType, @NonNull Generator<?> generator) {
        generatorProvidersMediator.setGeneratorByType(generatedType, generator);
    }

    void addGroups(String[] groups) {
        rulesInfoExtractor.getFieldsGroupFilter().includeGroups(groups);
    }

    private Function<String, DtoGeneratorBuildersTree.Node> nestedDtoGeneratorBuilderSupplier() {
        return fieldName -> {
            String[] pathToNestedDtoField =
                    Arrays.copyOf(pathFromDtoRoot, pathFromDtoRoot.length + 1);

            pathToNestedDtoField[pathFromDtoRoot.length] = fieldName;

            return dtoGeneratorBuildersTree.get().getNodeLazy(pathToNestedDtoField);
        };
    }

    private Optional<RuleInfo> getRuleInfo(Field field) {
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