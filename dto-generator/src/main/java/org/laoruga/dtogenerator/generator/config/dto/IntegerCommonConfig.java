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
public class IntegerCommonConfig implements ConfigDto {

    private final Map<Class<?>, IntegerConfig> map = new HashMap<>();

    @Getter
    @Setter
    @Accessors(chain = true)
    private RuleRemark ruleRemark;

    /*
     * Integer
     */

    public IntegerCommonConfig setMaxIntValue(int value) {
        return setMaxValue(Integer.class, AtomicInteger.class, value);
    }

    public IntegerCommonConfig setMinIntValue(int value) {
        return setMinValue(Integer.class, AtomicInteger.class, value);
    }

    public IntegerCommonConfig setRuleRemarkInt(RuleRemark value) {
        return setRuleRemark(Integer.class, AtomicInteger.class, value);
    }

    /*
     * Long
     */

    public IntegerCommonConfig setMaxLongValue(long value) {
        return setMaxValue(Long.class, AtomicLong.class, value);
    }

    public IntegerCommonConfig setMinLongValue(long value) {
        return setMinValue(Long.class, AtomicLong.class, value);
    }

    public IntegerCommonConfig setRuleRemarkLong(RuleRemark value) {
        return setRuleRemark(Long.class, AtomicLong.class, value);
    }

    /*
     * Short
     */

    public IntegerCommonConfig setMaxShortValue(short value) {
        return setMaxValue(Short.class, value);

    }

    public IntegerCommonConfig setMinShortValue(short value) {
        return setMinValue(Short.class, value);
    }

    public IntegerCommonConfig setRuleRemarkShort(RuleRemark value) {
        return setRuleRemark(Short.class, value);
    }

    /*
     * Byte
     */

    public IntegerCommonConfig setMaxByteValue(byte value) {
        return setMaxValue(Byte.class, value);
    }

    public IntegerCommonConfig setMinByteValue(byte value) {
        return setMinValue(Byte.class, value);
    }

    public IntegerCommonConfig setRuleRemarkByte(RuleRemark value) {
        return setRuleRemark(Byte.class, value);
    }

    /*
     * BigInteger
     */

    public IntegerCommonConfig setMaxBigIntValue(BigInteger value) {
        return setMaxValue(BigInteger.class, value);
    }

    public IntegerCommonConfig setMaxBigIntValue(String value) {
        return setMaxBigIntValue(new BigInteger(value));
    }

    public IntegerCommonConfig setMinBigIntValue(BigInteger value) {
        return setMinValue(BigInteger.class, value);
    }

    public IntegerCommonConfig setMinBigIntValue(String value) {
        return setMinBigIntValue(new BigInteger(value));
    }

    public IntegerCommonConfig setRuleRemarkBigInteger(RuleRemark value) {
        return setRuleRemark(BigInteger.class, value);
    }

    /*
     * Common setters one type
     */

    private IntegerCommonConfig setMaxValue(Class<?> type, Number maxValue) {
        map.putIfAbsent(type, new IntegerConfig());
        map.get(type).setMaxValue(maxValue);
        return this;
    }

    private IntegerCommonConfig setMinValue(Class<?> type, Number minValue) {
        map.putIfAbsent(type, new IntegerConfig());
        map.get(type).setMinValue(minValue);
        return this;
    }

    private IntegerCommonConfig setRuleRemark(Class<?> type, RuleRemark ruleRemark) {
        map.putIfAbsent(type, new IntegerConfig());
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Common setters two types
     */

    public IntegerCommonConfig setMaxValue(Class<?> type, Class<?> secondType, Number maxIntValue) {
        if (!map.containsKey(type)) {
            IntegerConfig configDto = new IntegerConfig();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setMaxValue(maxIntValue);
        return this;
    }

    public IntegerCommonConfig setMinValue(Class<?> type, Class<?> secondType, Number minIntValue) {
        if (!map.containsKey(type)) {
            IntegerConfig configDto = new IntegerConfig();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setMinValue(minIntValue);
        return this;
    }

    public IntegerCommonConfig setRuleRemark(Class<?> type, Class<?> secondType, RuleRemark ruleRemark) {
        if (!map.containsKey(type)) {
            IntegerConfig configDto = new IntegerConfig();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Getter
     */

    IntegerConfig getConfigOrNull(Class<? extends Number> generateType) {
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
