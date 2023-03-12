package org.laoruga.dtogenerator.generator.configs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

import java.util.HashMap;
import java.util.Map;

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
        map.putIfAbsent(Integer.class, new NumberConfigDto());
        map.get(Integer.class).setMaxValue(maxIntValue);
        return this;
    }

    public NumberCommonConfigDto setMinIntValue(int minIntValue) {
        map.putIfAbsent(Integer.class, new NumberConfigDto());
        map.get(Integer.class).setMinValue(minIntValue);
        return this;
    }

    public void setRuleRemarkInt(IRuleRemark ruleRemark) {
        map.putIfAbsent(Integer.class, new NumberConfigDto());
        map.get(Integer.class).setRuleRemark(ruleRemark);
    }

    public void setMaxLongValue(long maxLongValue) {
        map.putIfAbsent(Long.class, new NumberConfigDto());
        map.get(Long.class).setMaxValue(maxLongValue);
    }

    public void setMinLongValue(long minLongValue) {
        map.putIfAbsent(Long.class, new NumberConfigDto());
        map.get(Long.class).setMinValue(minLongValue);
    }

    public void setRuleRemarkLong(IRuleRemark ruleRemark) {
        map.putIfAbsent(Long.class, new NumberConfigDto());
        map.get(Long.class).setRuleRemark(ruleRemark);
    }

    public void setMaxShortValue(short maxShortValue) {
        map.putIfAbsent(Short.class, new NumberConfigDto());
        map.get(Short.class).setMaxValue(maxShortValue);
    }

    public void setMinShortValue(short minShortValue) {
        map.putIfAbsent(Short.class, new NumberConfigDto());
        map.get(Short.class).setMinValue(minShortValue);
    }

    public void setRuleRemarkShort(IRuleRemark ruleRemark) {
        map.putIfAbsent(Short.class, new NumberConfigDto());
        map.get(Short.class).setRuleRemark(ruleRemark);
    }

    public void setMaxByteValue(byte maxByteValue) {
        map.putIfAbsent(Byte.class, new NumberConfigDto());
        map.get(Byte.class).setMaxValue(maxByteValue);
    }

    public void setMinByteValue(byte minByteValue) {
        map.putIfAbsent(Byte.class, new NumberConfigDto());
        map.get(Byte.class).setMinValue(minByteValue);
    }

    public void setRuleRemarkByte(IRuleRemark ruleRemark) {
        map.putIfAbsent(Byte.class, new NumberConfigDto());
        map.get(Byte.class).setRuleRemark(ruleRemark);
    }

    NumberConfigDto getConfigOrNull(Class<? extends Number> generateType) {
        return map.get(generateType);
    }

    public void merge(ConfigDto configDto) {
        throw new NotImplementedException("Not supposed to be in use.");
    }
}
