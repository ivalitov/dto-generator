package org.laoruga.dtogenerator.generators;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.laoruga.dtogenerator.constants.BasicRuleRemark.NULL_VALUE;
import static org.laoruga.dtogenerator.util.ReflectionUtils.assertTypeCompatibility;
import static org.laoruga.dtogenerator.util.ReflectionUtils.createCollectionFieldInstance;

/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneratorsFactory {

    private static final GeneratorsFactory INSTANCE = new GeneratorsFactory();

    public static GeneratorsFactory getInstance() {
        return INSTANCE;
    }

    public IGenerator<?> getBasicTypeGenerator(String fieldName,
                                               Class<?> fieldType,
                                               Annotation rules,
                                               AtomicReference<IRuleRemark> maybeRemark) {
        Class<? extends Annotation> rulesClass = rules.annotationType();

        if (DoubleRule.class == rulesClass) {
            return getDoubleGenerator(fieldName, (DoubleRule) rules, fieldType, maybeRemark);
        }

        if (StringRule.class == rulesClass) {
            return getStringGenerator((StringRule) rules, fieldType, maybeRemark);
        }

        if (IntegerRule.class == rulesClass) {
            return getIntegerGenerator(fieldName, (IntegerRule) rules, fieldType, maybeRemark);
        }

        if (LongRule.class == rulesClass) {
            return getLongGenerator(fieldName, (LongRule) rules, fieldType, maybeRemark);
        }

        if (EnumRule.class == rulesClass) {
            return getEnumGenerator((EnumRule) rules, fieldType, maybeRemark);
        }

        if (LocalDateTimeRule.class == rulesClass) {
            return getLocalDateTimeGenerator((LocalDateTimeRule) rules, fieldType, maybeRemark);
        }

        throw new DtoGeneratorException("Field '" + fieldName + "' hasn't been mapped with any basic generator.");
    }

    public IGenerator<?> getCollectionTypeGenerator(String fieldName,
                                                    Class<?> fieldType,
                                                    Annotation rules,
                                                    IGenerator<?> itemGenerator,
                                                    AtomicReference<IRuleRemark> maybeRemark) {
        Class<? extends Annotation> rulesClass = rules.annotationType();
        if (rulesClass != ListRule.class && rulesClass != SetRule.class) {
        }

        if (ListRule.class == rulesClass) {
            ListRule listRule = (ListRule) rules;
            assertTypeCompatibility(fieldType, listRule.listClass());
            return getListGenerator(listRule, List.class, itemGenerator, maybeRemark);
        }

        if (SetRule.class == rulesClass) {
            SetRule setRule = (SetRule) rules;
            assertTypeCompatibility(fieldType, setRule.setClass());
            return getSetGenerator(setRule, Set.class, itemGenerator, maybeRemark);
        }

        throw new DtoGeneratorException("Field " + fieldName + " hasn't been mapped with any collection generator.");

    }

    /*
     * Basic type generators providers
     */

    private IGenerator<?> getStringGenerator(StringRule stringRule, Class<?> generatedType, AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark stringRuleRemark = maybeRemark.get() == null ? stringRule.ruleRemark() : maybeRemark.get();
        return ((StringGenerator.StringGeneratorBuilder) getBuilder(generatedType))
                .minLength(stringRule.minSymbols())
                .maxLength(stringRule.maxSymbols())
                .charset(stringRule.charset())
                .chars(stringRule.chars())
                .words(stringRule.words())
                .ruleRemark(stringRuleRemark)
                .regexp(stringRule.regexp())
                .build();
    }

    private IGenerator<?> getDoubleGenerator(String fieldName, DoubleRule doubleRule, Class<?> generatedType,
                                             AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? doubleRule.ruleRemark() : maybeRemark.get();
        if (remark == NULL_VALUE && generatedType == Double.TYPE) {
            reportPrimitiveCannotBeNull(fieldName);
            return (IGenerator<Double>) () -> 0D;
        }
        return ((DoubleGenerator.DoubleGeneratorBuilder) getBuilder(generatedType))
                .minValue(doubleRule.minValue())
                .maxValue(doubleRule.maxValue())
                .precision(doubleRule.precision())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getIntegerGenerator(String fieldName, IntegerRule integerRule, Class<?> generatedType,
                                              AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? integerRule.ruleRemark() : maybeRemark.get();
        if (remark == NULL_VALUE && generatedType == Integer.TYPE) {
            reportPrimitiveCannotBeNull(fieldName);
            return (IGenerator<Integer>) () -> 0;
        }
        return ((IntegerGenerator.IntegerGeneratorBuilder) getBuilder(generatedType))
                .minValue(integerRule.minValue())
                .maxValue(integerRule.maxValue())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getLongGenerator(String fieldName, LongRule longRule, Class<?> generatedType,
                                           AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? longRule.ruleRemark() : maybeRemark.get();
        if (remark == NULL_VALUE && generatedType == Long.TYPE) {
            reportPrimitiveCannotBeNull(fieldName);
            return (IGenerator<Long>) () -> 0L;
        }
        return ((LongGenerator.LongGeneratorBuilder) getBuilder(generatedType))
                .minValue(longRule.minValue())
                .maxValue(longRule.maxValue())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getEnumGenerator(EnumRule enumRule, Class<?> generatedType, AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? enumRule.ruleRemark() : maybeRemark.get();
        return ((EnumGenerator.EnumGeneratorBuilder) getBuilder(generatedType))
                .enumClass(enumRule.enumClass())
                .possibleEnumNames(enumRule.possibleEnumNames())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getLocalDateTimeGenerator(LocalDateTimeRule localDateTimeRule,
                                                    Class<?> generatedType,
                                                    AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? localDateTimeRule.ruleRemark() : maybeRemark.get();

        return ((LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder) getBuilder(generatedType))
                .leftShiftDays(localDateTimeRule.leftShiftDays())
                .rightShiftDays(localDateTimeRule.rightShiftDays())
                .ruleRemark(remark)
                .build();
    }

    /*
     * Collection generators providers
     */

    IGenerator<?> getListGenerator(ListRule listRule,
                                   Class<?> generatedType,
                                   IGenerator<?> listItemGenerator,
                                   AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? listRule.ruleRemark() : maybeRemark.get();
        return ((CollectionGenerator.CollectionGeneratorBuilder) getBuilder(generatedType))
                .minSize(listRule.minSize())
                .maxSize(listRule.maxSize())
                .listInstance(createCollectionFieldInstance(listRule.listClass()))
                .itemGenerator(listItemGenerator)
                .ruleRemark(remark)
                .build();
    }

    IGenerator<?> getSetGenerator(SetRule setRule,
                                  Class<?> generatedType,
                                  IGenerator<?> listItemGenerator,
                                  AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? setRule.ruleRemark() : maybeRemark.get();

        return ((CollectionGenerator.CollectionGeneratorBuilder) getBuilder(generatedType))
                .minSize(setRule.minSize())
                .maxSize(setRule.maxSize())
                .listInstance(createCollectionFieldInstance(setRule.setClass()))
                .itemGenerator(listItemGenerator)
                .ruleRemark(remark)
                .build();

    }

    /*
     * Utils
     */

    private static IGeneratorBuilder<?> getBuilder(Class<?> generatedType) {
        Optional<IGeneratorBuilder<?>> maybeBuilder = GeneratorBuildersProvider.getInstance().getBuilder(generatedType);
        if (maybeBuilder.isPresent()) {
            return maybeBuilder.get();
        } else {
            throw new DtoGeneratorException("Wrong configuration. '" + generatedType + "' generator builder not found");
        }
    }

    private void reportPrimitiveCannotBeNull(String fieldName) {
        log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
    }
}
