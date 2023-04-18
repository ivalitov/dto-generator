package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.RuleRemark;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@NoArgsConstructor
public class IntegralCommonConfig implements ConfigDto {

    private final Map<Class<?>, IntegralConfig> map = new HashMap<>();

    @Getter
    @Setter
    @Accessors(chain = true)
    private RuleRemark ruleRemark;

    /*
     * Integer
     */

    public IntegralCommonConfig setMaxIntValue(int value) {
        return setMaxValue(Integer.class, AtomicInteger.class, value);
    }

    public IntegralCommonConfig setMinIntValue(int value) {
        return setMinValue(Integer.class, AtomicInteger.class, value);
    }

    public IntegralCommonConfig setRuleRemarkInt(RuleRemark value) {
        return setRuleRemark(Integer.class, AtomicInteger.class, value);
    }

    /*
     * Long
     */

    public IntegralCommonConfig setMaxLongValue(long value) {
        return setMaxValue(Long.class, AtomicLong.class, value);
    }

    public IntegralCommonConfig setMinLongValue(long value) {
        return setMinValue(Long.class, AtomicLong.class, value);
    }

    public IntegralCommonConfig setRuleRemarkLong(RuleRemark value) {
        return setRuleRemark(Long.class, AtomicLong.class, value);
    }

    /*
     * Short
     */

    public IntegralCommonConfig setMaxShortValue(short value) {
        return setMaxValue(Short.class, value);

    }

    public IntegralCommonConfig setMinShortValue(short value) {
        return setMinValue(Short.class, value);
    }

    public IntegralCommonConfig setRuleRemarkShort(RuleRemark value) {
        return setRuleRemark(Short.class, value);
    }

    /*
     * Byte
     */

    public IntegralCommonConfig setMaxByteValue(byte value) {
        return setMaxValue(Byte.class, value);
    }

    public IntegralCommonConfig setMinByteValue(byte value) {
        return setMinValue(Byte.class, value);
    }

    public IntegralCommonConfig setRuleRemarkByte(RuleRemark value) {
        return setRuleRemark(Byte.class, value);
    }

    /*
     * BigInteger
     */

    public IntegralCommonConfig setMaxBigIntValue(BigInteger value) {
        return setMaxValue(BigInteger.class, value);
    }

    public IntegralCommonConfig setMaxBigIntValue(String value) {
        return setMaxBigIntValue(new BigInteger(value));
    }

    public IntegralCommonConfig setMinBigIntValue(BigInteger value) {
        return setMinValue(BigInteger.class, value);
    }

    public IntegralCommonConfig setMinBigIntValue(String value) {
        return setMinBigIntValue(new BigInteger(value));
    }

    public IntegralCommonConfig setRuleRemarkBigInteger(RuleRemark value) {
        return setRuleRemark(BigInteger.class, value);
    }

    /*
     * Common setters one type
     */

    private IntegralCommonConfig setMaxValue(Class<?> type, Number maxValue) {
        map.putIfAbsent(type, new IntegralConfig());
        map.get(type).setMaxValue(maxValue);
        return this;
    }

    private IntegralCommonConfig setMinValue(Class<?> type, Number minValue) {
        map.putIfAbsent(type, new IntegralConfig());
        map.get(type).setMinValue(minValue);
        return this;
    }

    private IntegralCommonConfig setRuleRemark(Class<?> type, RuleRemark ruleRemark) {
        map.putIfAbsent(type, new IntegralConfig());
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Common setters two types
     */

    public IntegralCommonConfig setMaxValue(Class<?> type, Class<?> secondType, Number maxIntValue) {
        if (!map.containsKey(type)) {
            IntegralConfig configDto = new IntegralConfig();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setMaxValue(maxIntValue);
        return this;
    }

    public IntegralCommonConfig setMinValue(Class<?> type, Class<?> secondType, Number minIntValue) {
        if (!map.containsKey(type)) {
            IntegralConfig configDto = new IntegralConfig();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setMinValue(minIntValue);
        return this;
    }

    public IntegralCommonConfig setRuleRemark(Class<?> type, Class<?> secondType, RuleRemark ruleRemark) {
        if (!map.containsKey(type)) {
            IntegralConfig configDto = new IntegralConfig();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Getter
     */

    IntegralConfig getConfigOrNull(Class<? extends Number> generateType) {
        return map.get(generateType);
    }

    /**
     * Not supposed to be in use, this class is just syntax sugar.
     */
    @Deprecated
    public void merge(ConfigDto configDto) {
        throw new NotImplementedException("Not supposed to be in use.");
    }
}
