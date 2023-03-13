package org.laoruga.dtogenerator.generator.configs;

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

    public NumberCommonConfigDto setMaxIntValue(int maxIntValue) {
        if (!map.containsKey(Integer.class)) {
            NumberConfigDto configDto = new NumberConfigDto();
            map.putIfAbsent(Integer.class, configDto);
            map.putIfAbsent(AtomicInteger.class, configDto);
        }
        map.get(Integer.class).setMaxValue(maxIntValue);
        return this;
    }

    public NumberCommonConfigDto setMinIntValue(int minIntValue) {
        if (!map.containsKey(Integer.class)) {
            NumberConfigDto configDto = new NumberConfigDto();
            map.putIfAbsent(Integer.class, configDto);
            map.putIfAbsent(AtomicInteger.class, configDto);
        }
        map.get(Integer.class).setMinValue(minIntValue);
        return this;
    }

    public NumberCommonConfigDto setRuleRemarkInt(IRuleRemark ruleRemark) {
        if (!map.containsKey(Integer.class)) {
            NumberConfigDto configDto = new NumberConfigDto();
            map.putIfAbsent(Integer.class, configDto);
            map.putIfAbsent(AtomicInteger.class, configDto);
        }
        map.get(Integer.class).setRuleRemark(ruleRemark);
        return this;
    }

    /*
     * Long
     */

    public NumberCommonConfigDto setMaxLongValue(long value) {
        return setMaxValue(Long.class, value);

    }

    public NumberCommonConfigDto setMinLongValue(long value) {
        return setMinValue(Long.class, value);
    }

    public NumberCommonConfigDto setRuleRemarkLong(IRuleRemark value) {
        return setRuleRemark(Long.class, value);
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
     * Common setters
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
