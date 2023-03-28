package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkableArgs;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.CustomGenerator;
import org.laoruga.dtogenerator.generator.MapGenerator;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;
import org.laoruga.dtogenerator.generator.supplier.GeneralGeneratorSuppliers;
import org.laoruga.dtogenerator.generator.supplier.GeneratorSuppliers;
import org.laoruga.dtogenerator.rule.IRuleInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.constants.RuleRemark.MIN_VALUE;
import static org.laoruga.dtogenerator.constants.RuleRemark.NULL_VALUE;
import static org.laoruga.dtogenerator.constants.RulesInstance.NUMBER_RULE_ZEROS;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByAnnotation extends GeneratorsProviderAbstract {

    private final GeneratorsProviderByType generatorsProviderByType;
    private final GeneratorSuppliers userGeneratorSuppliers;
    private final GeneratorSuppliers defaultGeneratorSuppliers;

    public GeneratorsProviderByAnnotation(ConfigurationHolder configuration,
                                          GeneratorsProviderByType generatorsProviderByType,
                                          RemarksHolder remarksHolder,
                                          GeneratorSuppliers userGeneratorSuppliers) {
        super(configuration, remarksHolder);
        this.generatorsProviderByType = generatorsProviderByType;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
        this.defaultGeneratorSuppliers = GeneralGeneratorSuppliers.getInstance();
    }

    IGenerator<?> getGenerator(IRuleInfo ruleInfo,
                               Supplier<?> dtoInstanceSupplier,
                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        String fieldName = ruleInfo.getField().getName();
        Class<?> requiredType = ruleInfo.getRequiredType();

        Optional<Function<ConfigDto, IGenerator<?>>> maybeUserGeneratorSupplier = getUserGeneratorSupplier(requiredType);

        boolean isUserGenerator = maybeUserGeneratorSupplier.isPresent();

        Function<ConfigDto, IGenerator<?>> generatorSupplier = isUserGenerator ?
                maybeUserGeneratorSupplier.get() :
                getDefaultGenBuilder(ruleInfo.getRule(), requiredType);

        ConfigDto config = getGeneratorConfig(
                ruleInfo.getRule(),
                requiredType,
                fieldName,
                dtoInstanceSupplier,
                nestedDtoGeneratorSupplier);

        return generatorSupplier.apply(config);
    }

    Function<ConfigDto, IGenerator<?>> getDefaultGenBuilder(Annotation rules, Class<?> generatedType) {
        Optional<Function<ConfigDto, IGenerator<?>>> maybeBuilder;
        switch (RuleType.getType(rules)) {
            case CUSTOM:
            case NESTED:
                maybeBuilder = defaultGeneratorSuppliers.getGeneratorSupplier(rules);
                break;
            default:
                maybeBuilder = defaultGeneratorSuppliers.getGeneratorSupplier(generatedType);
        }

        return maybeBuilder.orElseThrow(() ->
                new DtoGeneratorException("General generator builder not found. Rules: '"
                        + rules.annotationType().getName() + "', Genrated type: '" + generatedType.getName() + "'")
        );
    }

    Optional<Function<ConfigDto, IGenerator<?>>> getUserGeneratorSupplier(Class<?> generatedType) {
        return userGeneratorSuppliers.getGeneratorSupplier(generatedType);
    }

    ConfigDto getGeneratorConfig(Annotation rules,
                                           Class<?> fieldType,
                                           String fieldName,
                                           Supplier<?> dtoInstanceSupplier,
                                           Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        Class<? extends Annotation> rulesClass = rules.annotationType();

        try {

            if (BooleanRule.class == rulesClass) {

                return mergeGeneratorConfigurations(
                        () -> new BooleanConfigDto((BooleanRule) rules),
                        booleanGeneratorSpecificConfig(fieldType, fieldName),
                        fieldType,
                        fieldName);

            } else if (StringRule.class == rulesClass) {

                return mergeGeneratorConfigurations(
                        () -> new StringConfigDto((StringRule) rules),
                        fieldType,
                        fieldName);

            } else if (DecimalRule.class == rulesClass) {

                if (Number.class.isAssignableFrom(Primitives.wrap(fieldType))) {

                    @SuppressWarnings("unchecked")
                    Class<? extends Number> fieldTypeNumber = (Class<? extends Number>) fieldType;

                    return mergeGeneratorConfigurations(
                            () -> new DecimalConfigDto((DecimalRule) rules, fieldTypeNumber),
                            decimalGeneratorSpecificConfig(fieldType, fieldName),
                            fieldType,
                            fieldName);

                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType
                        + "' doesn't extend Number.class");

            } else if (NumberRule.class == rulesClass) {

                if (Number.class.isAssignableFrom(Primitives.wrap(fieldType))) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Number> fieldTypeNumber = (Class<? extends Number>) fieldType;

                    return mergeGeneratorConfigurations(
                            () -> new NumberConfigDto((NumberRule) rules, fieldTypeNumber),
                            integerGeneratorSpecificConfig(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType
                        + "' doesn't extend Number.class");

            } else if (EnumRule.class == rulesClass) {

                if (Enum.class.isAssignableFrom(fieldType)) {
                    return mergeGeneratorConfigurations(
                            () -> new EnumConfigDto((EnumRule) rules),
                            enumGeneratorSpecificConfig(fieldType),
                            fieldType,
                            fieldName
                    );
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Enum");

            } else if (DateTimeRule.class == rulesClass) {

                if (Temporal.class.isAssignableFrom(fieldType)) {

                    @SuppressWarnings("unchecked")
                    Class<? extends Temporal> fieldTypeTemporal = (Class<? extends Temporal>) fieldType;

                    return mergeGeneratorConfigurations(
                            () -> new DateTimeConfigDto((DateTimeRule) rules, fieldTypeTemporal),
                            fieldType,
                            fieldName);
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Temporal");

            } else if (CustomRule.class == rulesClass) {

                return CustomConfigDto.builder()
                        .customGeneratorRules(rules)
                        .dtoInstanceSupplier(dtoInstanceSupplier)
                        .build();

            } else if (NestedDtoRule.class == rulesClass) {

                return NestedConfigDto.builder()
                        .dtoGenerator(nestedDtoGeneratorSupplier.get())
                        .build();

            } else {

                throw new DtoGeneratorException("Unable to rules class: '" + rulesClass  + "'.");

            }

        } catch (Exception e) {
            throw new DtoGeneratorException("Unexpected error.", e);
        }
    }

    void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
        if (generator instanceof CustomGenerator) {
            IGenerator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();
            if (usersGeneratorInstance instanceof ICollectionGenerator) {

                prepareCustomRemarks(
                        ((ICollectionGenerator<?>) usersGeneratorInstance).getElementGenerator(),
                        fieldName
                );

            } else if (usersGeneratorInstance instanceof MapGenerator) {

                prepareCustomRemarks(
                        ((MapGenerator) usersGeneratorInstance).getKeyGenerator(),
                        fieldName
                );

                prepareCustomRemarks(
                        ((MapGenerator) usersGeneratorInstance).getValueGenerator(),
                        fieldName
                );

            } else if (usersGeneratorInstance instanceof ICustomGeneratorRemarkableArgs) {

                ((ICustomGeneratorRemarkableArgs<?>) usersGeneratorInstance).setRuleRemarks(
                        getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarksWithArgs(fieldName, usersGeneratorInstance.getClass()));

            } else if (usersGeneratorInstance instanceof ICustomGeneratorRemarkable) {

                ((ICustomGeneratorRemarkable<?>) usersGeneratorInstance).setRuleRemarks(
                        getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarks(fieldName, usersGeneratorInstance.getClass())
                );

            }
        }

    }

    IGenerator<?> getGeneratorByType(Field field, Class<?> generatedType) {
        Optional<IGenerator<?>> generatorByType = generatorsProviderByType.getGenerator(field, generatedType);

        if (!generatorByType.isPresent()) {
            throw new DtoGeneratorException("Generator wasn't found by type: '" + generatedType + "'" +
                    " for field: '" + field.getType() + " " + field.getName() + "'");
        }

        return generatorByType.get();
    }

    /*
     * Specific type configurations
     */

    @SuppressWarnings("unchecked")
    Consumer<ConfigDto> integerGeneratorSpecificConfig(Class<?> fieldType,
                                                       String fieldName) {
        return (config) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                config.merge(new NumberConfigDto(NUMBER_RULE_ZEROS, (Class<? extends Number>) fieldType)
                        .setRuleRemark(MIN_VALUE));
            }
        };
    }

    Consumer<ConfigDto> decimalGeneratorSpecificConfig(Class<?> fieldType,
                                                       String fieldName) {
        return (config) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                ((DecimalConfigDto) config)
                        .setMinValue(0D)
                        .setMaxValue(0D)
                        .setRuleRemark(MIN_VALUE);
            }
        };
    }

    Consumer<ConfigDto> booleanGeneratorSpecificConfig(Class<?> fieldType,
                                                       String fieldName) {
        return config -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                ((BooleanConfigDto) config).setTrueProbability(0D).setRuleRemark(MIN_VALUE);
            }
        };
    }

    /*
     * Utils
     */

    protected void reportPrimitiveCannotBeNull(String fieldName) {
        log.warn("Primitive field " + fieldName + " can't be null, it will be assigned to '0'");
    }

}
