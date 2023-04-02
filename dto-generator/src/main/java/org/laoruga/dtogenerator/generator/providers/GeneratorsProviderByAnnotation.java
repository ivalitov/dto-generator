package org.laoruga.dtogenerator.generator.providers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorRemarkableArgs;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.CustomGenerator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorByAnnotation;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSupplierInfo;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliersDefault;
import org.laoruga.dtogenerator.rule.RuleInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByAnnotation {

    @Getter
    private final GeneratorConfiguratorByAnnotation configuratorByAnnotation;
    private final GeneratorsProviderByType generatorsProviderByType;
    private final GeneratorSuppliers userGeneratorSuppliers;
    private final GeneratorSuppliers defaultGeneratorSuppliers;

    public GeneratorsProviderByAnnotation(GeneratorConfiguratorByAnnotation configuratorByAnnotation,
                                          GeneratorsProviderByType generatorsProviderByType,
                                          GeneratorSuppliers userGeneratorSuppliers) {
        this.configuratorByAnnotation = configuratorByAnnotation;
        this.generatorsProviderByType = generatorsProviderByType;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
        this.defaultGeneratorSuppliers = GeneratorSuppliersDefault.getInstance();
    }

    Generator<?> getGenerator(RuleInfo ruleInfo) {

        String fieldName = ruleInfo.getField().getName();
        Class<?> requiredType = ruleInfo.getRequiredType();

        Optional<Function<ConfigDto, Generator<?>>> maybeUserGeneratorSupplier = getUserGeneratorSupplier(requiredType);

        if (maybeUserGeneratorSupplier.isPresent()) {
            // user generators are not configurable yet
            return maybeUserGeneratorSupplier.get().apply(null);
        }

        ConfigDto config = configuratorByAnnotation.createGeneratorConfig(
                ruleInfo.getRule(),
                requiredType,
                fieldName
        );

        Generator<?> generator = getDefaultGeneratorSupplier(ruleInfo.getRule(), requiredType).apply(config);

        prepareCustomRemarks(generator, fieldName);

        return generator;
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

    Optional<Function<ConfigDto, Generator<?>>> getUserGeneratorSupplier(Class<?> generatedType) {
        return userGeneratorSuppliers
                .getGeneratorSupplierInfo(generatedType)
                .map(GeneratorSupplierInfo::getGeneratorSupplier);
    }


    void prepareCustomRemarks(Generator<?> generator, String fieldName) {
        if (generator instanceof CustomGenerator) {
            Generator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();

            if (usersGeneratorInstance instanceof CustomGeneratorRemarkableArgs) {

                ((CustomGeneratorRemarkableArgs<?>) usersGeneratorInstance).setRuleRemarks(
                        configuratorByAnnotation.getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarksWithArgs(fieldName, usersGeneratorInstance.getClass()));

            } else if (usersGeneratorInstance instanceof CustomGeneratorRemarkable) {

                ((CustomGeneratorRemarkable<?>) usersGeneratorInstance).setRuleRemarks(
                        configuratorByAnnotation.getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarks(fieldName, usersGeneratorInstance.getClass())
                );

            }
        }

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
