package org.laoruga.dtogenerator.typegenerators.providers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.TypeGeneratorRemarksProvider;
import org.laoruga.dtogenerator.TypeGeneratorsProvider;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.rules.IRuleInfo;
import org.laoruga.dtogenerator.rules.RuleInfoCollection;
import org.laoruga.dtogenerator.typegenerators.*;
import org.laoruga.dtogenerator.typegenerators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.typegenerators.builders.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.constants.RuleRemark.NULL_VALUE;
import static org.laoruga.dtogenerator.util.ReflectionUtils.createCollectionInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorBuildersProviderByAnnotation extends AbstractGeneratorBuildersProvider {

    private final GeneratorBuildersProviderByType generatorBuildersProviderByType;
    private final TypeGeneratorRemarksProvider typeGeneratorRemarksProvider;
    private final GeneratorBuildersHolder userGeneratorBuildersHolder;
    private final GeneratorBuildersHolder defaultGeneratorBuildersHolder = GeneratorBuildersHolderGeneral.getInstance();

    @Setter
    volatile private Field field;
    @Setter
    volatile private Object dtoInstance;
    @Setter
    volatile private IRuleInfo ruleInfo;
    @Setter
    volatile private Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier;
    volatile private Class<?> generatedTypeOrCollectionElementType;

    public GeneratorBuildersProviderByAnnotation(DtoGeneratorInstanceConfig configuration,
                                                 GeneratorBuildersProviderByType generatorBuildersProviderByType,
                                                 TypeGeneratorRemarksProvider typeGeneratorRemarksProvider,
                                                 GeneratorBuildersHolder userGeneratorBuildersHolder) {
        super(configuration);
        this.generatorBuildersProviderByType = generatorBuildersProviderByType;
        this.typeGeneratorRemarksProvider = typeGeneratorRemarksProvider;
        this.userGeneratorBuildersHolder = userGeneratorBuildersHolder;
    }

    public Optional<IGenerator<?>> selectOrCreateGenerator() {

        IGenerator<?> generator;

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {

            generatedTypeOrCollectionElementType = ReflectionUtils.getSingleGenericType(field);

            RuleInfoCollection collectionRuleInfo = (RuleInfoCollection) ruleInfo;

            // Collection generator builder

            Optional<IGeneratorBuilder> maybeCollectionUserGenBuilder = getUsersGenBuilder(
                    collectionRuleInfo.getRule(),
                    getFieldType());

            boolean isUserCollectionBuilder = maybeCollectionUserGenBuilder.isPresent();

            IGeneratorBuilder collectionGenBuilder = isUserCollectionBuilder ?
                    maybeCollectionUserGenBuilder.get() :
                    getDefaultGenBuilder(
                            collectionRuleInfo.getRule(),
                            getFieldType());

            // Collection element generator builder

            IGenerator<?> elementGenerator;
            if (collectionRuleInfo.isElementRulesExist()) {
                IRuleInfo elementRuleInfo = collectionRuleInfo.getElementRule();

                Optional<IGeneratorBuilder> maybeUsersElementGenBuilder = getUsersGenBuilder(
                        elementRuleInfo.getRule(),
                        generatedTypeOrCollectionElementType);

                boolean isUserBuilder = maybeUsersElementGenBuilder.isPresent();

                IGeneratorBuilder elementGenBuilder = isUserBuilder ?
                        maybeUsersElementGenBuilder.get() :
                        getDefaultGenBuilder(
                                elementRuleInfo.getRule(),
                                generatedTypeOrCollectionElementType);

                elementGenerator = buildGenerator(
                        elementRuleInfo.getRule(),
                        elementGenBuilder,
                        false);

            } else {

                Optional<IGenerator<?>> maybeGenerator = generatorBuildersProviderByType
                        .selectOrCreateGenerator(generatedTypeOrCollectionElementType);

                if (!maybeGenerator.isPresent()) {
                    throw new DtoGeneratorException("Collection element rules absent on the field," +
                            " and element generator wasn't evaluated by type.");
                }

                elementGenerator = maybeGenerator.get();
            }

            prepareCustomRemarks(elementGenerator, getFieldName());

            generator = buildCollectionGenerator(
                    collectionRuleInfo.getRule(),
                    collectionGenBuilder,
                    elementGenerator,
                    isUserCollectionBuilder
            );

        } else {

            generatedTypeOrCollectionElementType = getFieldType();

            Optional<IGeneratorBuilder> maybeUsersGenBuilder = getUsersGenBuilder(
                    ruleInfo.getRule(),
                    getFieldType());

            boolean isUserBuilder = maybeUsersGenBuilder.isPresent();
            IGeneratorBuilder genBuilder = isUserBuilder ?
                    maybeUsersGenBuilder.get() :
                    getDefaultGenBuilder(
                            ruleInfo.getRule(),
                            getFieldType());

            generator = buildGenerator(
                    ruleInfo.getRule(),
                    genBuilder,
                    generatedTypeOrCollectionElementType.isPrimitive());
        }

        prepareCustomRemarks(generator, getFieldName());

        return Optional.ofNullable(generator);
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
                                         boolean isPrimitive) {

        Class<? extends Annotation> rulesClass = rules.annotationType();


        try {


            if (StringRule.class == rulesClass) {

                if (generatorBuilder instanceof StringGenerator.StringGeneratorBuilder) {

                    return getGenerator(
                            () -> new StringGenerator.ConfigDto((StringRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            getFieldType());
                }

            } else if (DoubleRule.class == rulesClass) {

                if (generatorBuilder instanceof DoubleGenerator.DoubleGeneratorBuilder) {

                    return getGenerator(
                            () -> new DoubleGenerator.ConfigDto((DoubleRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> (config.getRuleRemark() == NULL_VALUE && isPrimitive) ?
                                    () -> {
                                        reportPrimitiveCannotBeNull();
                                        return 0D;
                                    } :
                                    builder.build(config, true),
                            getFieldType());
                }

            } else if (IntegerRule.class == rulesClass) {

                if (generatorBuilder instanceof IntegerGenerator.IntegerGeneratorBuilder) {

                    return getGenerator(
                            () -> new IntegerGenerator.ConfigDto((IntegerRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> (config.getRuleRemark() == NULL_VALUE && isPrimitive) ?
                                    () -> {
                                        reportPrimitiveCannotBeNull();
                                        return 0;
                                    } :
                                    builder.build(config, true),
                            getFieldType());
                }

            } else if (LongRule.class == rulesClass) {

                if (generatorBuilder instanceof LongGenerator.LongGeneratorBuilder) {

                    return getGenerator(
                            () -> new LongGenerator.ConfigDto((LongRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> (config.getRuleRemark() == NULL_VALUE && isPrimitive) ?
                                    () -> {
                                        reportPrimitiveCannotBeNull();
                                        return 0;
                                    } :
                                    builder.build(config, true),
                            getFieldType());
                }

            } else if (EnumRule.class == rulesClass) {

                if (generatorBuilder instanceof EnumGenerator.EnumGeneratorBuilder) {
                    return getGenerator(
                            () -> new EnumGenerator.ConfigDto((EnumRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            enumGeneratorSupplier(generatedTypeOrCollectionElementType),
                            getFieldType()
                    );
                }

            } else if (LocalDateTimeRule.class == rulesClass) {

                if (generatorBuilder instanceof LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder) {
                    return getGenerator(
                            () -> new LocalDateTimeGenerator.ConfigDto((LocalDateTimeRule) rules),
                            () -> (IGeneratorBuilderConfigurable) generatorBuilder,
                            (config, builder) -> builder.build(config, true),
                            getFieldType());
                }

            } else if (CustomRule.class == rulesClass) {

                if (generatorBuilder instanceof CustomGenerator.CustomGeneratorBuilder) {
                    return getCustomGenerator(
                            ((CustomGenerator.CustomGeneratorBuilder) generatorBuilder),
                            (CustomRule) rules);
                }

            } else if (NestedDtoRule.class == rulesClass) {

                if (generatorBuilder instanceof NestedDtoGenerator.NestedDtoGeneratorBuilder) {
                    return getNestedDtoGenerator(
                            ((NestedDtoGenerator.NestedDtoGeneratorBuilder) generatorBuilder));
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
                                                   boolean isUserCollectionBuilder) {
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
                    getFieldType());
        }

        log.debug("Unknown collection builder builds as is, without Rules annotation params passing.");

        return collectionGenBuilder.build();
    }

    private IGenerator<?> getCustomGenerator(CustomGenerator.CustomGeneratorBuilder builder,
                                             CustomRule rule) {
        return builder
                .setCustomGeneratorRules(rule)
                .setDtoInstance(dtoInstance)
                .build();
    }

    private IGenerator<?> getNestedDtoGenerator(NestedDtoGenerator.NestedDtoGeneratorBuilder builder) {
        return builder
                .setNestedDtoGeneratorSupplier(nestedDtoGeneratorSupplier)
                .build();
    }

    private void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
        if (generator instanceof CustomGenerator) {
            IGenerator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();
            if (usersGeneratorInstance instanceof ICollectionGenerator) {
                prepareCustomRemarks(((ICollectionGenerator<?>) usersGeneratorInstance).getElementGenerator(), fieldName);
            }
            if (usersGeneratorInstance instanceof ICustomGeneratorRemarkable) {
                ICustomGeneratorRemarkable<?> remarkableGenerator = (ICustomGeneratorRemarkable<?>) usersGeneratorInstance;
                typeGeneratorRemarksProvider.getRemarks(fieldName, remarkableGenerator)
                        .ifPresent(remarkableGenerator::setRuleRemarks);
            }
        }

    }

    private String getFieldName() {
        return field.getName();
    }

    private Class<?> getFieldType() {
        return field.getType();
    }

    /*
     * Utils
     */

    private void reportPrimitiveCannotBeNull() {
        log.debug("Primitive field " + getFieldName() + " can't be null, it will be assigned to '0'");
    }

    @Override
    public void accept(TypeGeneratorsProvider<?>.ProvidersVisitor visitor) {
        super.accept(visitor);
        generatorBuildersProviderByType.accept(visitor);
    }
}
