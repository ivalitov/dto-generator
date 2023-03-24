package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkableArgs;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.*;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;
import org.laoruga.dtogenerator.rule.IRuleInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.BiFunction;
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
    private final GeneratorBuildersHolder userGeneratorBuildersHolder;
    private final GeneratorBuildersHolder defaultGeneratorBuildersHolder;

    public GeneratorsProviderByAnnotation(ConfigurationHolder configuration,
                                          GeneratorsProviderByType generatorsProviderByType,
                                          RemarksHolder remarksHolder,
                                          GeneratorBuildersHolder userGeneratorBuildersHolder) {
        super(configuration, remarksHolder);
        this.generatorsProviderByType = generatorsProviderByType;
        this.userGeneratorBuildersHolder = userGeneratorBuildersHolder;
        this.defaultGeneratorBuildersHolder = GeneratorBuildersHolderGeneral.getInstance();
    }

    IGenerator<?> getGenerator(IRuleInfo ruleInfo,
                               Supplier<?> dtoInstanceSupplier,
                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        String fieldName = ruleInfo.getField().getName();
        Class<?> requiredType = ruleInfo.getRequiredType();

        Optional<IGeneratorBuilder<?>> maybeUsersKeyGenBuilder = getUsersGenBuilder(requiredType);

        boolean isUserBuilder = maybeUsersKeyGenBuilder.isPresent();

        IGeneratorBuilder<?> generatorBuilder = isUserBuilder ?
                maybeUsersKeyGenBuilder.get() :
                getDefaultGenBuilder(ruleInfo.getRule(), requiredType);

        return buildGenerator(
                ruleInfo.getRule(),
                generatorBuilder,
                requiredType,
                fieldName,
                dtoInstanceSupplier,
                nestedDtoGeneratorSupplier);
    }

    protected IGeneratorBuilder<?> getDefaultGenBuilder(Annotation rules, Class<?> generatedType) {
        Optional<IGeneratorBuilder<?>> maybeBuilder;
        switch (RuleType.getType(rules)) {
            case CUSTOM:
            case NESTED:
                maybeBuilder = defaultGeneratorBuildersHolder.getBuilder(rules);
                break;
            default:
                maybeBuilder = defaultGeneratorBuildersHolder.getBuilder(generatedType);
        }

        return maybeBuilder.orElseThrow(() ->
                new DtoGeneratorException("General generator builder not found. Rules: '"
                        + rules.annotationType().getName() + "', Genrated type: '" + generatedType.getName() + "'")
        );
    }

    protected Optional<IGeneratorBuilder<?>> getUsersGenBuilder(Class<?> generatedType) {
        return userGeneratorBuildersHolder.getBuilder(generatedType);
    }

    protected IGenerator<?> buildGenerator(Annotation rules,
                                           final IGeneratorBuilder<?> generatorBuilder,
                                           Class<?> fieldType,
                                           String fieldName,
                                           Supplier<?> dtoInstanceSupplier,
                                           Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        Class<? extends Annotation> rulesClass = rules.annotationType();

        try {

            if (BooleanRule.class == rulesClass && generatorBuilder instanceof BooleanGeneratorBuilder) {

                return getGenerator(
                        () -> new BooleanConfigDto((BooleanRule) rules),
                        () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                        booleanGeneratorSupplier(fieldType, fieldName),
                        fieldType,
                        fieldName);

            } else if (StringRule.class == rulesClass && generatorBuilder instanceof StringGeneratorBuilder) {

                return getGenerator(
                        () -> new StringConfigDto((StringRule) rules),
                        () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                        (config, builder) -> builder.build(config, true),
                        fieldType,
                        fieldName);

            } else if (DecimalRule.class == rulesClass
                    && generatorBuilder instanceof DecimalGeneratorBuilder) {

                if (Number.class.isAssignableFrom(Primitives.wrap(fieldType))) {

                    @SuppressWarnings("unchecked")
                    Class<? extends Number> fieldTypeNumber = (Class<? extends Number>) fieldType;

                    return getGenerator(
                            () -> new DecimalConfigDto((DecimalRule) rules, fieldTypeNumber),
                            () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                            doubleGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);

                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType
                        + "' doesn't extend Number.class");

            } else if (NumberRule.class == rulesClass
                    && generatorBuilder instanceof NumberGeneratorBuilder) {

                if (Number.class.isAssignableFrom(Primitives.wrap(fieldType))) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Number> fieldTypeNumber = (Class<? extends Number>) fieldType;

                    return getGenerator(
                            () -> new NumberConfigDto((NumberRule) rules, fieldTypeNumber),
                            () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                            integerGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType
                        + "' doesn't extend Number.class");

            } else if (EnumRule.class == rulesClass && generatorBuilder instanceof EnumGeneratorBuilder) {

                if (Enum.class.isAssignableFrom(fieldType)) {
                    return getGenerator(
                            () -> new EnumConfigDto((EnumRule) rules),
                            () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                            getEnumGeneratorSupplier(fieldType),
                            fieldType,
                            fieldName
                    );
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Enum");

            } else if (DateTimeRule.class == rulesClass && generatorBuilder instanceof DateTimeGeneratorBuilder) {

                if (Temporal.class.isAssignableFrom(fieldType)) {

                    @SuppressWarnings("unchecked")
                    Class<? extends Temporal> fieldTypeTemporal = (Class<? extends Temporal>) fieldType;

                    return getGenerator(
                            () -> new DateTimeConfigDto((DateTimeRule) rules, fieldTypeTemporal),
                            () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            fieldType,
                            fieldName);
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Temporal");

            } else if (CustomRule.class == rulesClass && generatorBuilder instanceof CustomGeneratorBuilder) {
                return ((CustomGeneratorBuilder) generatorBuilder)
                        .setCustomGeneratorRules(rules)
                        .setDtoInstanceSupplier(dtoInstanceSupplier)
                        .build();

            } else if (NestedDtoRule.class == rulesClass && generatorBuilder instanceof NestedDtoGeneratorBuilder) {
                return ((NestedDtoGeneratorBuilder) generatorBuilder)
                        .setNestedDtoGeneratorSupplier(nestedDtoGeneratorSupplier)
                        .build();

            } else {

                if (generatorBuilder instanceof IGeneratorBuilderConfigurable) {
                    return getGenerator(
                            TypeGeneratorsDefaultConfigSupplier.getDefaultConfigSupplier(
                                    Primitives.wrap(fieldType)
                            ),
                            () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            fieldType,
                            fieldName);
                }

                log.debug("Unknown generator builder, trying to build 'as is' without configuring.");
                return generatorBuilder.build();

            }

        } catch (Exception e) {
            throw new DtoGeneratorException("Unexpected error.", e);
        }
    }

    protected void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
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
     * Implementation of functional interfaces for code readability
     */

    @SuppressWarnings("unchecked")
    BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> integerGeneratorSupplier(Class<?> fieldType,
                                                                                                    String fieldName) {
        return (config, builder) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                return
                        NumberGenerator.builder(
                                        new NumberConfigDto(NUMBER_RULE_ZEROS, (Class<? extends Number>) fieldType))
                                .ruleRemark(MIN_VALUE)
                                .build();
            }
            return builder.build(config, true);
        };
    }

    BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> doubleGeneratorSupplier(Class<?> fieldType,
                                                                                                   String fieldName) {
        return (config, builder) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                return DecimalGenerator.builder()
                        .minValue(0D)
                        .maxValue(0D)
                        .ruleRemark(MIN_VALUE).build();
            }
            return builder.build(config, true);
        };
    }

    BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> booleanGeneratorSupplier(Class<?> fieldType,
                                                                                                    String fieldName) {
        return (config, builder) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                return BooleanGenerator.builder()
                        .trueProbability(0D)
                        .ruleRemark(MIN_VALUE).build();
            }
            return builder.build(config, true);
        };
    }

    /*
     * Utils
     */

    protected void reportPrimitiveCannotBeNull(String fieldName) {
        log.warn("Primitive field " + fieldName + " can't be null, it will be assigned to '0'");
    }

}
