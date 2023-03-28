package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

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
public class NumberCommonConfigDto implements ConfigDto {

    private final Map<Class<?>, NumberConfigDto> map = new HashMap<>();

    @Getter
    @Setter
    @Accessors(chain = true)
    private IRuleRemark ruleRemark;

    /*
     * Integer
     */

    public NumberCommonConfigDto setMaxIntValue(int value) {
        return setMaxValue(Integer.class, AtomicInteger.class, value);
    }

    public NumberCommonConfigDto setMinIntValue(int value) {
        return setMinValue(Integer.class, AtomicInteger.class, value);
    }

    public NumberCommonConfigDto setRuleRemarkInt(IRuleRemark value) {
        return setRuleRemark(Integer.class, AtomicInteger.class, value);
    }

    /*
     * Long
     */

    public NumberCommonConfigDto setMaxLongValue(long value) {
        return setMaxValue(Long.class, AtomicLong.class, value);
    }

    public NumberCommonConfigDto setMinLongValue(long value) {
        return setMinValue(Long.class, AtomicLong.class, value);
    }

    public NumberCommonConfigDto setRuleRemarkLong(IRuleRemark value) {
        return setRuleRemark(Long.class, AtomicLong.class, value);
    }

    /*
     * Short
     */

    public NumberCommonConfigDto setMaxShortValue(short value) {
        return setMaxValue(Short.class, value);

    }

    public NumberCommonConfigDto setMinShortValue(short value) {
        return setMinValue(Short.class, value);
    }

    public NumberCommonConfigDto setRuleRemarkShort(IRuleRemark value) {
        return setRuleRemark(Short.class, value);
    }

    /*
     * Byte
     */

    public NumberCommonConfigDto setMaxByteValue(byte value) {
        return setMaxValue(Byte.class, value);
    }

    public NumberCommonConfigDto setMinByteValue(byte value) {
        return setMinValue(Byte.class, value);
    }

    public NumberCommonConfigDto setRuleRemarkByte(IRuleRemark value) {
        return setRuleRemark(Byte.class, value);
    }

    /*
     * BigInteger
     */

    public NumberCommonConfigDto setMaxBigIntValue(BigInteger value) {
        return setMaxValue(BigInteger.class, value);
    }

    public NumberCommonConfigDto setMaxBigIntValue(String value) {
        return setMaxBigIntValue(new BigInteger(value));
    }

    public NumberCommonConfigDto setMinBigIntValue(BigInteger value) {
        return setMinValue(BigInteger.class, value);
    }

    public NumberCommonConfigDto setMinBigIntValue(String value) {
        return setMinBigIntValue(new BigInteger(value));
    }

    public NumberCommonConfigDto setRuleRemarkBigInteger(IRuleRemark value) {
        return setRuleRemark(BigInteger.class, value);
    }

    /*
     * Common setters one type
     */

    private NumberCommonConfigDto setMaxValue(Class<?> type, Number maxValue) {
        map.putIfAbsent(type, new NumberConfigDto());
        map.get(type).setMaxValue(maxValue);
        return this;
    }

    private NumberCommonConfigDto setMinValue(Class<?> type, Number minValue) {
        map.putIfAbsent(type, new NumberConfigDto());
        map.get(type).setMinValue(minValue);
        return this;
    }

    private NumberCommonConfigDto setRuleRemark(Class<?> type, IRuleRemark ruleRemark) {
        map.putIfAbsent(type, new NumberConfigDto());
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Common setters two types
     */

    public NumberCommonConfigDto setMaxValue(Class<?> type, Class<?> secondType, Number maxIntValue) {
        if (!map.containsKey(type)) {
            NumberConfigDto configDto = new NumberConfigDto();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setMaxValue(maxIntValue);
        return this;
    }

    public NumberCommonConfigDto setMinValue(Class<?> type, Class<?> secondType, Number minIntValue) {
        if (!map.containsKey(type)) {
            NumberConfigDto configDto = new NumberConfigDto();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setMinValue(minIntValue);
        return this;
    }

    public NumberCommonConfigDto setRuleRemark(Class<?> type, Class<?> secondType, IRuleRemark ruleRemark) {
        if (!map.containsKey(type)) {
            NumberConfigDto configDto = new NumberConfigDto();
            map.putIfAbsent(type, configDto);
            map.putIfAbsent(secondType, configDto);
        }
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Getter
     */

    NumberConfigDto getConfigOrNull(Class<? extends Number> generateType) {
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
