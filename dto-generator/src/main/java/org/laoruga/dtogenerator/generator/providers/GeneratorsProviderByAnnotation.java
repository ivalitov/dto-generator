package org.laoruga.dtogenerator.generator.providers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGeneratorBuildersTree;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.config.CustomGeneratorsConfigurationHolder;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.NestedDtoGenerator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorByAnnotation;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.config.dto.NestedConfig;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSupplierInfo;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliersDefault;
import org.laoruga.dtogenerator.generator.providers.suppliers.UserGeneratorSuppliers;
import org.laoruga.dtogenerator.rule.RuleInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByAnnotation {

    @Getter
    private final GeneratorConfiguratorByAnnotation configuratorByAnnotation;
    private final GeneratorsProviderByType generatorsProviderByType;
    private final UserGeneratorSuppliers userGeneratorSuppliers;
    private final GeneratorSuppliers defaultGeneratorSuppliers;

    private final Function<String, DtoGeneratorBuildersTree.Node> nestedDtoGeneratorBuilderSupplier;


    public GeneratorsProviderByAnnotation(GeneratorConfiguratorByAnnotation configuratorByAnnotation,
                                          GeneratorsProviderByType generatorsProviderByType,
                                          UserGeneratorSuppliers userGeneratorSuppliers,
                                          Function<String, DtoGeneratorBuildersTree.Node> nestedDtoGeneratorBuilderSupplier) {
        this.configuratorByAnnotation = configuratorByAnnotation;
        this.generatorsProviderByType = generatorsProviderByType;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
        this.defaultGeneratorSuppliers = GeneratorSuppliersDefault.getInstance();
        this.nestedDtoGeneratorBuilderSupplier = nestedDtoGeneratorBuilderSupplier;
    }

    @SuppressWarnings("unchecked")
    Generator<?> getGenerator(RuleInfo ruleInfo) {

        String fieldName = ruleInfo.getField().getName();
        Class<?> requiredType = ruleInfo.getRequiredType();

        Optional<Generator<?>> maybeUserGenerator =
                userGeneratorSuppliers.getGenerator(requiredType);

        if (maybeUserGenerator.isPresent()) {

            Generator<?> generator = maybeUserGenerator.get();

            if (generator instanceof CustomGenerator) {
                configuratorByAnnotation.getConfiguration()
                        .getCustomGeneratorsConfigurators()
                        .getBuilder(
                                ruleInfo.getField().getName(),
                                (Class<? extends CustomGenerator<?>>) generator.getClass()
                        )
                        .build()
                        .configure((CustomGenerator<?>) generator);
            }

            return generator;
        }

        Annotation rule = ruleInfo.getRule();

        if (CustomRule.class == rule.annotationType()) {
            return createCustomGenerator((CustomRule) rule, fieldName);
        }

        if (NestedDtoRule.class == rule.annotationType()) {
            return createNestedGenerator((NestedDtoRule) rule, fieldName);
        }

        ConfigDto config = configuratorByAnnotation.createGeneratorConfig(
                rule,
                requiredType,
                fieldName
        );

        return getDefaultGeneratorSupplier(rule, requiredType)
                .apply(config);
    }

    Generator<?> createCustomGenerator(CustomRule customRule, String fieldName) {

        CustomGeneratorsConfigurationHolder configurators = configuratorByAnnotation
                .getConfiguration()
                .getCustomGeneratorsConfigurators();

        Class<? extends CustomGenerator<?>> generatorClass = customRule.generatorClass();

        CustomGenerator<?> generatorInstance = createInstance(generatorClass);

        configurators.getBuilder(fieldName, generatorClass, customRule.args())
                .boundary(customRule.boundary())
                .build()
                .configure(generatorInstance);

        return generatorInstance;
    }

    Generator<?> createNestedGenerator(NestedDtoRule nestedRule, String fieldName) {
        return new NestedDtoGenerator(
                NestedConfig.builder()
                        .ruleRemark(nestedRule.boundary())
                        .dtoGeneratorBuilderTreeNode(nestedDtoGeneratorBuilderSupplier.apply(fieldName))
                        .build()
        );
    }


    Function<ConfigDto, Generator<?>> getDefaultGeneratorSupplier(Annotation rules, Class<?> generatedType) {
        Optional<Function<ConfigDto, Generator<?>>> maybeBuilder;
        switch (RuleType.getType(rules)) {
            case CUSTOM:
            case NESTED:
                maybeBuilder = defaultGeneratorSuppliers
                        .getGeneratorSupplierInfo(rules)
                        .map(GeneratorSupplierInfo::getGeneratorSupplier);
                break;

            default:
                maybeBuilder = defaultGeneratorSuppliers
                        .getGeneratorSupplierInfo(generatedType)
                        .map(GeneratorSupplierInfo::getGeneratorSupplier);
        }

        return maybeBuilder.orElseThrow(() ->
                new DtoGeneratorException("General generator builder not found. Rules: '"
                        + rules.annotationType().getName() + "', Genrated type: '" + generatedType.getName() + "'")
        );
    }

    Generator<?> getGeneratorByType(Field field, Class<?> generatedType) {
        Optional<Generator<?>> generatorByType = generatorsProviderByType.getGenerator(field, generatedType);

        if (!generatorByType.isPresent()) {
            throw new DtoGeneratorException("Generator wasn't found by type: '" + generatedType + "'" +
                    " for field: '" + field.getType() + " " + field.getName() + "'");
        }

        return generatorByType.get();
    }

}
