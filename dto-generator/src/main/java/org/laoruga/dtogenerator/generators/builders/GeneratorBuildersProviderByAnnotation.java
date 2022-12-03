package org.laoruga.dtogenerator.generators.builders;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.*;
import org.laoruga.dtogenerator.api.generators.*;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.RulesInstance;
import org.laoruga.dtogenerator.generators.StaticGeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.laoruga.dtogenerator.constants.BasicRuleRemark.NULL_VALUE;
import static org.laoruga.dtogenerator.util.ReflectionUtils.createCollectionInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorBuildersProviderByAnnotation extends AbstractGeneratorBuildersProvider {
    public GeneratorBuildersProviderByAnnotation(DtoGeneratorInstanceConfig configuration,
                                                 GeneratorBuildersProviderByType generatorBuildersProviderByType,
                                                 GeneratorRemarksProvider generatorRemarksProvider,
                                                 GeneratorBuildersHolder userGeneratorBuildersHolder) {
        super(configuration);
        this.generatorBuildersProviderByType = generatorBuildersProviderByType;
        this.generatorRemarksProvider = generatorRemarksProvider;
        this.userGeneratorBuildersHolder = userGeneratorBuildersHolder;
    }

    private final GeneratorBuildersProviderByType generatorBuildersProviderByType;
    private final GeneratorRemarksProvider generatorRemarksProvider;
    private final GeneratorBuildersHolder userGeneratorBuildersHolder;
    private final GeneratorBuildersHolder defaultlGeneratorBuildersHolder = StaticGeneratorBuildersHolder.getInstance();

    @Setter
    volatile private Field field;
    @Setter
    volatile private Object dtoInstance;
    @Setter
    volatile private IRuleInfo ruleInfo;
    @Setter
    volatile private DtoGeneratorBuilder.GeneratorBuildersTree genBuildersTree;
    @Setter
    volatile private String[] fieldPathFromRoot;
    volatile private Class<?> generatedTypeOrCollectionElementType;

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
                        false,
                        isUserBuilder);

            } else {

                Optional<IGenerator<?>> maybeGenerator = generatorBuildersProviderByType
                        .selectOrCreateGenerator(generatedTypeOrCollectionElementType);

                if (!maybeGenerator.isPresent()) {
                    throw new DtoGeneratorException("Collection element rules absent on the field," +
                            " and element generator wasn't evaluated by type.");
                }

                elementGenerator = maybeGenerator.get();
            }

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
                    generatedTypeOrCollectionElementType.isPrimitive(),
                    isUserBuilder);
        }

        prepareCustomRemarks(generator, getFieldName());

        return Optional.ofNullable(generator);
    }

    private IGeneratorBuilder getDefaultGenBuilder(Annotation rules, Class<?> generatedType) {
        return defaultlGeneratorBuildersHolder.getBuilder(rules, generatedType)
                .orElseThrow(() -> new DtoGeneratorException("General generator builder not found. Rules: '" + rules + "'"
                        + ", Genrated type: '" + generatedType + "'"));
    }

    private Optional<IGeneratorBuilder> getUsersGenBuilder(Annotation rules, Class<?> generatedType) {
        return userGeneratorBuildersHolder.getBuilder(rules, generatedType);
    }

    private IGenerator<?> buildGenerator(Annotation rules,
                                         IGeneratorBuilder generatorBuilder,
                                         boolean isPrimitive,
                                         boolean userBuilder) {

        Class<? extends Annotation> rulesClass = rules.annotationType();


        try {
            if (generatorBuilder instanceof IGeneratorBuilderConfigurable) {

                IGeneratorBuilderConfigurable genBuilderConfigurable = (IGeneratorBuilderConfigurable) generatorBuilder;

                if (StringRule.class == rulesClass) {

                    if (genBuilderConfigurable instanceof StringGenerator.StringGeneratorBuilder) {

                        return getGenerator(
                                () -> userBuilder ?
                                        new StringGenerator.ConfigDto(RulesInstance.stringRule) :
                                        new StringGenerator.ConfigDto((StringRule) rules),
                                () -> genBuilderConfigurable,
                                (config, builder) -> ((StringGenerator.StringGeneratorBuilder) builder)
                                        .build(config, true));

                    }

                } else if (DoubleRule.class == rulesClass) {

                    if (genBuilderConfigurable instanceof DoubleGenerator.DoubleGeneratorBuilder) {

                        return getGenerator(
                                () -> userBuilder ?
                                        new DoubleGenerator.ConfigDto(RulesInstance.doubleRule) :
                                        new DoubleGenerator.ConfigDto((DoubleRule) rules),
                                () -> genBuilderConfigurable,
                                (config, builder) -> (config.getRuleRemark() == NULL_VALUE && isPrimitive) ?
                                        () -> {
                                            reportPrimitiveCannotBeNull();
                                            return 0D;
                                        } :
                                        ((DoubleGenerator.DoubleGeneratorBuilder) builder).build(config, true));
                    }

                } else if (IntegerRule.class == rulesClass) {

                    if (genBuilderConfigurable instanceof IntegerGenerator.IntegerGeneratorBuilder) {

                        return getGenerator(
                                () -> userBuilder ?
                                        new IntegerGenerator.ConfigDto(RulesInstance.integerRule) :
                                        new IntegerGenerator.ConfigDto((IntegerRule) rules),
                                () -> genBuilderConfigurable,
                                (config, builder) -> (config.getRuleRemark() == NULL_VALUE && isPrimitive) ?
                                        () -> {
                                            reportPrimitiveCannotBeNull();
                                            return 0;
                                        } :
                                        ((IntegerGenerator.IntegerGeneratorBuilder) builder).build(config, true));
                    }

                } else if (LongRule.class == rulesClass) {

                    if (genBuilderConfigurable instanceof LongGenerator.LongGeneratorBuilder) {

                        return getGenerator(
                                () -> userBuilder ?
                                        new LongGenerator.ConfigDto(RulesInstance.longRule) :
                                        new LongGenerator.ConfigDto((LongRule) rules),
                                () -> genBuilderConfigurable,
                                (config, builder) -> (config.getRuleRemark() == NULL_VALUE && isPrimitive) ?
                                        () -> {
                                            reportPrimitiveCannotBeNull();
                                            return 0;
                                        } :
                                        ((LongGenerator.LongGeneratorBuilder) builder).build(config, true));
                    }

                } else if (EnumRule.class == rulesClass) {

                    if (genBuilderConfigurable instanceof EnumGenerator.EnumGeneratorBuilder) {
                        return getGenerator(
                                () -> userBuilder ?
                                        new EnumGenerator.ConfigDto(RulesInstance.enumRule) :
                                        new EnumGenerator.ConfigDto((EnumRule) rules),
                                () -> genBuilderConfigurable,
                                enumGeneratorSupplier(generatedTypeOrCollectionElementType)
                        );
                    }

                } else if (LocalDateTimeRule.class == rulesClass) {

                    if (genBuilderConfigurable instanceof LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder) {
                        return getGenerator(
                                () -> userBuilder ?
                                        new LocalDateTimeGenerator.ConfigDto(RulesInstance.localDateTimeRule) :
                                        new LocalDateTimeGenerator.ConfigDto((LocalDateTimeRule) rules),
                                () -> genBuilderConfigurable,
                                (config, builder) -> ((LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder) builder).build(config, true));
                    }

                }
            }

            if (CustomRule.class == rulesClass) {

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

        log.debug("Unknown generator builder. It builds as is, without passing params .");
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

                configDto = (isUserCollectionBuilder ?

                        new CollectionGenerator.ConfigDto(RulesInstance.listRule)
                                .setCollectionInstance(
                                        () -> createCollectionInstance((RulesInstance.listRule).listClass())) :

                        new CollectionGenerator.ConfigDto((ListRule) collectionRule))
                        .setCollectionInstance(
                                () -> createCollectionInstance(((ListRule) collectionRule).listClass()));

            } else if (SetRule.class == rulesClass) {

                configDto = isUserCollectionBuilder ?

                        new CollectionGenerator.ConfigDto(RulesInstance.setRule)
                                .setCollectionInstance(
                                        () -> createCollectionInstance((RulesInstance.setRule).setClass())) :

                        new CollectionGenerator.ConfigDto((SetRule) collectionRule)
                                .setCollectionInstance(
                                        () -> createCollectionInstance(((SetRule) collectionRule).setClass()));

            } else {
                throw new DtoGeneratorException("Unknown rules annotation class '" + rulesClass + "'");
            }

            return getGenerator(
                    () -> configDto,
                    () -> (IGeneratorBuilderConfigurable) collectionGenBuilder,
                    collectionGeneratorSupplier(elementGenerator));
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
                .setGeneratorBuildersTree(genBuildersTree)
                .setField(field)
                .setFieldsPath(fieldPathFromRoot)
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
                generatorRemarksProvider.getRemarks(fieldName, remarkableGenerator)
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
    public void accept(GeneratorsProvider<?>.ProvidersVisitor visitor) {
        super.accept(visitor);
        generatorBuildersProviderByType.accept(visitor);
    }
}
