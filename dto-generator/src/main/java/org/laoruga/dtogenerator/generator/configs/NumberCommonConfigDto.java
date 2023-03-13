package org.laoruga.dtogenerator.generator.configs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

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

    public NumberCommonConfigDto setMaxLongValue(long maxLongValue) {
        map.putIfAbsent(Long.class, new NumberConfigDto());
        map.get(Long.class).setMaxValue(maxLongValue);
        return this;
    }

    public NumberCommonConfigDto setMinLongValue(long minLongValue) {
        map.putIfAbsent(Long.class, new NumberConfigDto());
        map.get(Long.class).setMinValue(minLongValue);
        return this;
    }

    public NumberCommonConfigDto setRuleRemarkLong(IRuleRemark ruleRemark) {
        map.putIfAbsent(Long.class, new NumberConfigDto());
        map.get(Long.class).setRuleRemark(ruleRemark);
        return this;
    }

    public NumberCommonConfigDto setMaxShortValue(short maxShortValue) {
        map.putIfAbsent(Short.class, new NumberConfigDto());
        map.get(Short.class).setMaxValue(maxShortValue);
        return this;
    }

    public NumberCommonConfigDto setMinShortValue(short minShortValue) {
        map.putIfAbsent(Short.class, new NumberConfigDto());
        map.get(Short.class).setMinValue(minShortValue);
        return this;
    }

    public NumberCommonConfigDto setRuleRemarkShort(IRuleRemark ruleRemark) {
        map.putIfAbsent(Short.class, new NumberConfigDto());
        map.get(Short.class).setRuleRemark(ruleRemark);
        return this;
    }

    public NumberCommonConfigDto setMaxByteValue(byte maxByteValue) {
        map.putIfAbsent(Byte.class, new NumberConfigDto());
        map.get(Byte.class).setMaxValue(maxByteValue);
        return this;
    }

    public NumberCommonConfigDto setMinByteValue(byte minByteValue) {
        map.putIfAbsent(Byte.class, new NumberConfigDto());
        map.get(Byte.class).setMinValue(minByteValue);
        return this;
    }

    public NumberCommonConfigDto setRuleRemarkByte(IRuleRemark ruleRemark) {
        map.putIfAbsent(Byte.class, new NumberConfigDto());
        map.get(Byte.class).setRuleRemark(ruleRemark);
        return this;
    }

    NumberConfigDto getConfigOrNull(Class<? extends Number> generateType) {
        return map.get(generateType);
    }

    public void merge(ConfigDto configDto) {
        throw new NotImplementedException("Not supposed to be in use.");
    }
}
