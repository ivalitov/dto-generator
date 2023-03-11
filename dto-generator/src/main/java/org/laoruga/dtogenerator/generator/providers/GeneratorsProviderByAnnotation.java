package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.Getter;
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
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.BooleanGenerator;
import org.laoruga.dtogenerator.generator.CustomGenerator;
import org.laoruga.dtogenerator.generator.DoubleGenerator;
import org.laoruga.dtogenerator.generator.NumberGenerator;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.rule.IRuleInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.constants.RuleRemark.MIN_VALUE;
import static org.laoruga.dtogenerator.constants.RuleRemark.NULL_VALUE;
import static org.laoruga.dtogenerator.rule.RulesInstance.NUMBER_RULE_ZEROS;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
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

    protected IGenerator<?> getGenerator(Field field,
                                         IRuleInfo ruleInfo,
                                         Supplier<?> dtoInstanceSupplier,
                                         Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        Optional<IGeneratorBuilder<?>> maybeUsersGenBuilder = getUsersGenBuilder(
                ruleInfo.getRule(),
                fieldType);

        boolean isUserBuilder = maybeUsersGenBuilder.isPresent();
        IGeneratorBuilder<?> genBuilder = isUserBuilder ?
                maybeUsersGenBuilder.get() :
                getDefaultGenBuilder(
                        ruleInfo.getRule(),
                        fieldType);

        return buildGenerator(
                ruleInfo.getRule(),
                genBuilder,
                fieldType,
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

        return maybeBuilder.orElseThrow(() -> new DtoGeneratorException("General generator builder not found. Rules: '"
                + rules.annotationType().getName() + "', Genrated type: '" + generatedType.getName() + "'"));
    }

    protected Optional<IGeneratorBuilder<?>> getUsersGenBuilder(Annotation rules, Class<?> generatedType) {
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

            } else if (DoubleRule.class == rulesClass && generatorBuilder instanceof DoubleGeneratorBuilder) {

                return getGenerator(
                        () -> new DoubleConfigDto((DoubleRule) rules),
                        () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                        doubleGeneratorSupplier(fieldType, fieldName),
                        fieldType,
                        fieldName);

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

                throw new IllegalArgumentException("Unexpected state. Field type " + fieldType + " doesn't extend Number.class");

            } else if (EnumRule.class == rulesClass && generatorBuilder instanceof EnumGeneratorBuilder) {

                if (Enum.class.isAssignableFrom(fieldType)) {
                    return getGenerator(
                            () -> new EnumConfigDto((EnumRule) rules),
                            () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                            enumGeneratorSupplier(fieldType),
                            fieldType,
                            fieldName
                    );
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Enum");

            } else if (LocalDateTimeRule.class == rulesClass && generatorBuilder instanceof LocalDateTimeGeneratorBuilder) {
                return getGenerator(
                        () -> new LocalDateTimeConfigDto((LocalDateTimeRule) rules),
                        () -> (IGeneratorBuilderConfigurable<?>) generatorBuilder,
                        (config, builder) -> builder.build(config, true),
                        fieldType,
                        fieldName);

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
            if (e.getClass() == ClassCastException.class) {
                log.debug("Probably unknown builder, trying to build generator as is.");
                return generatorBuilder.build();
            }
            throw e;
        }


    }

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
                return DoubleGenerator.builder()
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

    protected void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
        if (generator instanceof CustomGenerator) {
            IGenerator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();
            if (usersGeneratorInstance instanceof ICollectionGenerator) {

                prepareCustomRemarks(((ICollectionGenerator) usersGeneratorInstance)
                        .getElementGenerator(), fieldName);

            } else if (usersGeneratorInstance instanceof ICustomGeneratorRemarkableArgs) {

                ((ICustomGeneratorRemarkableArgs<?>) usersGeneratorInstance).setRuleRemarks(
                        getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarksWithArgs(fieldName, usersGeneratorInstance.getClass()));

            } else if (usersGeneratorInstance instanceof ICustomGeneratorRemarkable) {

                ((ICustomGeneratorRemarkable<?>) usersGeneratorInstance).setRuleRemarks(
                        getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarks(fieldName, usersGeneratorInstance.getClass()));

            }
        }

    }

    /*
     * Utils
     */

    protected void reportPrimitiveCannotBeNull(String fieldName) {
        log.warn("Primitive field " + fieldName + " can't be null, it will be assigned to '0'");
    }

}
