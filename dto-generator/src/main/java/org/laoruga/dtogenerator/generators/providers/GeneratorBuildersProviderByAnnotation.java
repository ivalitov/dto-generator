package org.laoruga.dtogenerator.generators.providers;

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
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.*;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersFactory;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.rules.IRuleInfo;
import org.laoruga.dtogenerator.rules.RuleInfoCollection;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.constants.RuleRemark.MIN_VALUE;
import static org.laoruga.dtogenerator.constants.RuleRemark.NULL_VALUE;
import static org.laoruga.dtogenerator.util.ReflectionUtils.createCollectionInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorBuildersProviderByAnnotation extends AbstractGeneratorBuildersProvider {

    private final GeneratorBuildersProviderByType generatorBuildersProviderByType;
    private final GeneratorBuildersHolder userGeneratorBuildersHolder;
    private final GeneratorBuildersHolder defaultGeneratorBuildersHolder = GeneratorBuildersHolderGeneral.getInstance();

    public GeneratorBuildersProviderByAnnotation(DtoGeneratorInstanceConfig configuration,
                                                 GeneratorBuildersProviderByType generatorBuildersProviderByType,
                                                 RemarksHolder remarksHolder,
                                                 GeneratorBuildersHolder userGeneratorBuildersHolder) {
        super(configuration, remarksHolder);
        this.generatorBuildersProviderByType = generatorBuildersProviderByType;
        this.userGeneratorBuildersHolder = userGeneratorBuildersHolder;
    }


    public IGenerator<?> getGenerator(Field field,
                                      IRuleInfo ruleInfo,
                                      Supplier<?> dtoInstanceSupplier,
                                      Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        IGenerator<?> generator;

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {

            Class<?> collectionElementType = ReflectionUtils.getSingleGenericType(field);

            RuleInfoCollection collectionRuleInfo = (RuleInfoCollection) ruleInfo;

            // Collection generator builder

            Optional<IGeneratorBuilder> maybeCollectionUserGenBuilder = getUsersGenBuilder(
                    collectionRuleInfo.getRule(),
                    fieldType);

            boolean isUserCollectionBuilder = maybeCollectionUserGenBuilder.isPresent();

            IGeneratorBuilder collectionGenBuilder = isUserCollectionBuilder ?
                    maybeCollectionUserGenBuilder.get() :
                    getDefaultGenBuilder(
                            collectionRuleInfo.getRule(),
                            fieldType);

            // Collection element generator builder

            IGenerator<?> elementGenerator;
            if (collectionRuleInfo.isElementRulesExist()) {
                IRuleInfo elementRuleInfo = collectionRuleInfo.getElementRule();

                Optional<IGeneratorBuilder> maybeUsersElementGenBuilder = getUsersGenBuilder(
                        elementRuleInfo.getRule(),
                        collectionElementType);

                boolean isUserBuilder = maybeUsersElementGenBuilder.isPresent();

                IGeneratorBuilder elementGenBuilder = isUserBuilder ?
                        maybeUsersElementGenBuilder.get() :
                        getDefaultGenBuilder(
                                elementRuleInfo.getRule(),
                                collectionElementType);

                elementGenerator = buildGenerator(
                        elementRuleInfo.getRule(),
                        elementGenBuilder,
                        collectionElementType,
                        fieldName,
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier);

            } else {

                Optional<IGenerator<?>> maybeGenerator = generatorBuildersProviderByType
                        .getGenerator(field, collectionElementType);

                if (!maybeGenerator.isPresent()) {
                    throw new DtoGeneratorException("Collection element rules absent on the field," +
                            " and element generator wasn't evaluated by type.");
                }

                elementGenerator = maybeGenerator.get();
            }

            prepareCustomRemarks(elementGenerator, fieldName);

            generator = buildCollectionGenerator(
                    collectionRuleInfo.getRule(),
                    collectionGenBuilder,
                    elementGenerator,
                    fieldType,
                    fieldName
            );

        } else {

            Optional<IGeneratorBuilder> maybeUsersGenBuilder = getUsersGenBuilder(
                    ruleInfo.getRule(),
                    fieldType);

            boolean isUserBuilder = maybeUsersGenBuilder.isPresent();
            IGeneratorBuilder genBuilder = isUserBuilder ?
                    maybeUsersGenBuilder.get() :
                    getDefaultGenBuilder(
                            ruleInfo.getRule(),
                            fieldType);

            generator = buildGenerator(
                    ruleInfo.getRule(),
                    genBuilder,
                    fieldType,
                    fieldName,
                    dtoInstanceSupplier,
                    nestedDtoGeneratorSupplier);
        }

        prepareCustomRemarks(generator, fieldName);

        return generator;

    }

    private IGeneratorBuilder getDefaultGenBuilder(Annotation rules, Class<?> generatedType) {
        return defaultGeneratorBuildersHolder.getBuilder(rules, generatedType)
                .orElseThrow(() -> new DtoGeneratorException("General generator builder not found. Rules: '" + rules + "'"
                        + ", Genrated type: '" + generatedType + "'"));
    }

    private Optional<IGeneratorBuilder> getUsersGenBuilder(Annotation rules, Class<?> generatedType) {
        return userGeneratorBuildersHolder.getBuilder(rules, generatedType);
    }

    private IGenerator<?> buildGenerator(Annotation rules,
                                         IGeneratorBuilder generatorBuilder,
                                         Class<?> fieldType,
                                         String fieldName,
                                         Supplier<?> dtoInstanceSupplier,
                                         Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        Class<? extends Annotation> rulesClass = rules.annotationType();

        try {

            if (StringRule.class == rulesClass) {

                if (generatorBuilder instanceof StringGenerator.StringGeneratorBuilder) {

                    return getGenerator(
                            () -> new StringGenerator.ConfigDto((StringRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            fieldType,
                            fieldName);
                }

            } else if (DoubleRule.class == rulesClass) {

                if (generatorBuilder instanceof DoubleGenerator.DoubleGeneratorBuilder) {

                    return getGenerator(
                            () -> new DoubleGenerator.ConfigDto((DoubleRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            doubleGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

            } else if (IntegerRule.class == rulesClass) {

                if (generatorBuilder instanceof IntegerGenerator.IntegerGeneratorBuilder) {

                    return getGenerator(
                            () -> new IntegerGenerator.ConfigDto((IntegerRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            integerGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

            } else if (LongRule.class == rulesClass) {

                if (generatorBuilder instanceof LongGenerator.LongGeneratorBuilder) {

                    return getGenerator(
                            () -> new LongGenerator.ConfigDto((LongRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            longGeneratorSupplier(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

            } else if (EnumRule.class == rulesClass) {

                if (generatorBuilder instanceof EnumGenerator.EnumGeneratorBuilder) {
                    return getGenerator(
                            () -> new EnumGenerator.ConfigDto((EnumRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            enumGeneratorSupplier(fieldType),
                            fieldType,
                            fieldName
                    );
                }

            } else if (LocalDateTimeRule.class == rulesClass) {

                if (generatorBuilder instanceof LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder) {
                    return getGenerator(
                            () -> new LocalDateTimeGenerator.ConfigDto((LocalDateTimeRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            fieldType,
                            fieldName);
                }

            } else if (CustomRule.class == rulesClass) {

                if (generatorBuilder instanceof CustomGenerator.CustomGeneratorBuilder) {
                    return ((CustomGenerator.CustomGeneratorBuilder) generatorBuilder)
                            .setCustomGeneratorRules(rules)
                            .setDtoInstanceSupplier(dtoInstanceSupplier)
                            .build();
                }

            } else if (NestedDtoRule.class == rulesClass) {

                if (generatorBuilder instanceof NestedDtoGenerator.NestedDtoGeneratorBuilder) {
                    return ((NestedDtoGenerator.NestedDtoGeneratorBuilder) generatorBuilder)
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

    private IGenerator<?> buildCollectionGenerator(Annotation collectionRule,
                                                   IGeneratorBuilder collectionGenBuilder,
                                                   IGenerator<?> elementGenerator,
                                                   Class<?> fieldType,
                                                   String fieldName) {
        Class<? extends Annotation> rulesClass = collectionRule.annotationType();

        if (collectionGenBuilder instanceof CollectionGenerator.CollectionGeneratorBuilder) {

            CollectionGenerator.ConfigDto configDto;

            if (ListRule.class == rulesClass) {

                configDto = new CollectionGenerator.ConfigDto((ListRule) collectionRule)
                        .setCollectionInstance(
                                () -> createCollectionInstance(((ListRule) collectionRule).listClass()));

            } else if (SetRule.class == rulesClass) {

                configDto = new CollectionGenerator.ConfigDto((SetRule) collectionRule)
                        .setCollectionInstance(
                                () -> createCollectionInstance(((SetRule) collectionRule).setClass()));

            } else {
                throw new DtoGeneratorException("Unknown rules annotation class '" + rulesClass + "'");
            }

            return getGenerator(
                    () -> configDto,
                    () -> (IGeneratorBuilderConfigurable) collectionGenBuilder,
                    collectionGeneratorSupplier(elementGenerator),
                    fieldType,
                    fieldName);
        }

        log.debug("Unknown collection builder builds as is, without Rules annotation params passing.");

        return collectionGenBuilder.build();
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

    private void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
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

    private void reportPrimitiveCannotBeNull(String fieldName) {
        log.warn("Primitive field " + fieldName + " can't be null, it will be assigned to '0'");
    }

}
