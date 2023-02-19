package org.laoruga.dtogenerator.generator.providers;

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
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.CustomGenerator;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersFactory;
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

    public GeneratorsProviderByAnnotation(DtoGeneratorInstanceConfig configuration,
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

        Optional<IGeneratorBuilder> maybeUsersGenBuilder = getUsersGenBuilder(
                ruleInfo.getRule(),
                fieldType);

        boolean isUserBuilder = maybeUsersGenBuilder.isPresent();
        IGeneratorBuilder genBuilder = isUserBuilder ?
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

    protected IGeneratorBuilder getDefaultGenBuilder(Annotation rules, Class<?> generatedType) {
        return defaultGeneratorBuildersHolder.getBuilder(rules, generatedType)
                .orElseThrow(() -> new DtoGeneratorException("General generator builder not found. Rules: '"
                        + rules + "', Genrated type: '" + generatedType + "'"));
    }

    protected Optional<IGeneratorBuilder> getUsersGenBuilder(Annotation rules, Class<?> generatedType) {
        return userGeneratorBuildersHolder.getBuilder(rules, generatedType);
    }

    protected IGenerator<?> buildGenerator(Annotation rules,
                                           IGeneratorBuilder generatorBuilder,
                                           Class<?> fieldType,
                                           String fieldName,
                                           Supplier<?> dtoInstanceSupplier,
                                           Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        Class<? extends Annotation> rulesClass = rules.annotationType();

        try {

            if (StringRule.class == rulesClass) {

                if (generatorBuilder instanceof StringGeneratorBuilder) {

                    return getGenerator(
                            () -> new StringConfigDto((StringRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            fieldType,
                            fieldName);
                }

            } else if (DoubleRule.class == rulesClass) {

                if (generatorBuilder instanceof DoubleGeneratorBuilder) {

                    return getGenerator(
                            () -> new DoubleConfigDto((DoubleRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            doubleGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

            } else if (IntegerRule.class == rulesClass) {

                if (generatorBuilder instanceof IntegerGeneratorBuilder) {

                    return getGenerator(
                            () -> new IntegerConfigDto((IntegerRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            integerGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

            } else if (LongRule.class == rulesClass) {

                if (generatorBuilder instanceof LongGeneratorBuilder) {

                    return getGenerator(
                            () -> new LongConfigDto((LongRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            longGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

            } else if (EnumRule.class == rulesClass) {

                if (generatorBuilder instanceof EnumGeneratorBuilder) {
                    return getGenerator(
                            () -> new EnumConfigDto((EnumRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            enumGeneratorSupplier(fieldType),
                            fieldType,
                            fieldName
                    );
                }

            } else if (LocalDateTimeRule.class == rulesClass) {

                if (generatorBuilder instanceof LocalDateTimeGeneratorBuilder) {
                    return getGenerator(
                            () -> new LocalDateTimeConfigDto((LocalDateTimeRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            fieldType,
                            fieldName);
                }

            } else if (CustomRule.class == rulesClass) {

                if (generatorBuilder instanceof CustomGeneratorBuilder) {
                    return ((CustomGeneratorBuilder) generatorBuilder)
                            .setCustomGeneratorRules(rules)
                            .setDtoInstanceSupplier(dtoInstanceSupplier)
                            .build();
                }

            } else if (NestedDtoRule.class == rulesClass) {

                if (generatorBuilder instanceof NestedDtoGeneratorBuilder) {
                    return ((NestedDtoGeneratorBuilder) generatorBuilder)
                            .setNestedDtoGeneratorSupplier(nestedDtoGeneratorSupplier)
                            .build();
                }

            } else {
                throw new DtoGeneratorException("Unknown rules annotation '" + rulesClass + "'");
            }

        } catch (Exception e) {
            if (e.getClass() == ClassCastException.class) {
                log.debug("Probably unknown builder, trying to build generator as is.");
                return generatorBuilder.build();
            }
            throw e;
        }

        log.debug("Unknown generator builder, trying to build 'as is' without configuring.");
        return generatorBuilder.build();
    }

    BiFunction<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> integerGeneratorSupplier(Class<?> fieldType,
                                                                                                  String fieldName) {
        return (config, builder) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                return GeneratorBuildersFactory.integerBuilder()
                        .minValue(0)
                        .maxValue(0)
                        .ruleRemark(MIN_VALUE).build();
            }
            return builder.build(config, true);
        };
    }

    BiFunction<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> longGeneratorSupplier(Class<?> fieldType,
                                                                                               String fieldName) {
        return (config, builder) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                return GeneratorBuildersFactory.longBuilder()
                        .minValue(0L)
                        .maxValue(0L)
                        .ruleRemark(MIN_VALUE).build();
            }
            return builder.build(config, true);
        };
    }

    BiFunction<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> doubleGeneratorSupplier(Class<?> fieldType,
                                                                                                 String fieldName) {
        return (config, builder) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                return GeneratorBuildersFactory.doubleBuilder()
                        .minValue(0D)
                        .maxValue(0D)
                        .ruleRemark(MIN_VALUE).build();
            }
            return builder.build(config, true);
        };
    }

    protected void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
        if (generator instanceof CustomGenerator) {
            IGenerator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();
            if (usersGeneratorInstance instanceof ICollectionGenerator) {

                prepareCustomRemarks(((ICollectionGenerator<?>) usersGeneratorInstance)
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
