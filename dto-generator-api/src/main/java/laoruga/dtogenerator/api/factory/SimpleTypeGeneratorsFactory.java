package laoruga.dtogenerator.api.factory;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicReference;

import static laoruga.dtogenerator.api.constants.BasicRuleRemark.NULL_VALUE;

/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Slf4j
public class SimpleTypeGeneratorsFactory {

    private static final SimpleTypeGeneratorsFactory INSTANCE = new SimpleTypeGeneratorsFactory();

    public static SimpleTypeGeneratorsFactory getInstance() {
        return INSTANCE;
    }

    public IGenerator<?> getBasicTypeGenerator(String fieldName,
                                               Class<?> fieldType,
                                               Annotation rules,
                                               AtomicReference<IRuleRemark> maybeRemark) {
        Class<? extends Annotation> rulesClass = rules.annotationType();

        if (DoubleRule.class == rulesClass) {
            return getDoubleGenerator(fieldName, (DoubleRule) rules, fieldType == Double.TYPE, maybeRemark);
        }

        if (StringRule.class == rulesClass) {
            return getStringGenerator((StringRule) rules, maybeRemark);
        }

        if (IntegerRule.class == rulesClass) {
            return getIntegerGenerator(fieldName, (IntegerRule) rules, fieldType == Integer.TYPE, maybeRemark);
        }

        if (LongRule.class == rulesClass) {
            return getLongGenerator(fieldName, (LongRule) rules, fieldType == Integer.TYPE, maybeRemark);
        }

        if (EnumRule.class == rulesClass) {
            return getEnumGenerator((EnumRule) rules, maybeRemark);
        }

        if (LocalDateTimeRule.class == rulesClass) {
            return getLocalDateTimeGenerator((LocalDateTimeRule) rules, maybeRemark);
        }

        throw new DtoGeneratorException("Field '" + fieldName + "' hasn't been mapped with any basic generator.");
    }

    /*
     * Basic type generators providers
     */

    private IGenerator<?> getStringGenerator(StringRule stringRule, AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark stringRuleRemark = maybeRemark.get() == null ? stringRule.ruleRemark() : maybeRemark.get();
        return BasicGeneratorsBuilders.stringBuilder()
                .minLength(stringRule.minSymbols())
                .maxLength(stringRule.maxSymbols())
                .charset(stringRule.charset())
                .chars(stringRule.chars())
                .ruleRemark(stringRuleRemark)
                .regexp(stringRule.regexp())
                .build();

    }

    private IGenerator<?> getDoubleGenerator(String fieldName, DoubleRule doubleRule, boolean isPrimitive,
                                             AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? doubleRule.ruleRemark() : maybeRemark.get();
        if (remark == NULL_VALUE && isPrimitive) {
            reportPrimitiveCannotBeNull(fieldName);
            return (IGenerator<Double>) () -> 0D;
        }
        return BasicGeneratorsBuilders.doubleBuilder()
                .minValue(doubleRule.minValue())
                .maxValue(doubleRule.maxValue())
                .precision(doubleRule.precision())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getIntegerGenerator(String fieldName, IntegerRule integerRule, boolean isPrimitive,
                                              AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? integerRule.ruleRemark() : maybeRemark.get();
        if (remark == NULL_VALUE && isPrimitive) {
            reportPrimitiveCannotBeNull(fieldName);
            return (IGenerator<Integer>) () -> 0;
        }
        return BasicGeneratorsBuilders.integerBuilder()
                .minValue(integerRule.minValue())
                .maxValue(integerRule.maxValue())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getLongGenerator(String fieldName, LongRule longRule, boolean isPrimitive,
                                           AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? longRule.ruleRemark() : maybeRemark.get();
        if (remark == NULL_VALUE && isPrimitive) {
            reportPrimitiveCannotBeNull(fieldName);
            return (IGenerator<Long>) () -> 0L;
        }
        return BasicGeneratorsBuilders.longBuilder()
                .minValue(longRule.minValue())
                .maxValue(longRule.maxValue())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getEnumGenerator(EnumRule enumRule, AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? enumRule.ruleRemark() : maybeRemark.get();
        return BasicGeneratorsBuilders.enumBuilder()
                .enumClass(enumRule.enumClass())
                .possibleEnumNames(enumRule.possibleEnumNames())
                .ruleRemark(remark)
                .build();
    }

    private IGenerator<?> getLocalDateTimeGenerator(LocalDateTimeRule localDateTimeRule,
                                                    AtomicReference<IRuleRemark> maybeRemark) {
        IRuleRemark remark = maybeRemark.get() == null ? localDateTimeRule.ruleRemark() : maybeRemark.get();

        return BasicGeneratorsBuilders.localDateTimeBuilder()
                .leftShiftDays(localDateTimeRule.leftShiftDays())
                .rightShiftDays(localDateTimeRule.rightShiftDays())
                .ruleRemark(remark)
                .build();
    }

    private void reportPrimitiveCannotBeNull(String fieldName){
        log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
    }
}
