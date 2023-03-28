package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@NoArgsConstructor
public class DecimalCommonConfigDto implements ConfigDto {

    @Getter
    @Setter
    @Accessors(chain = true)
    private IRuleRemark ruleRemark;

    private final Map<Class<?>, DecimalConfigDto> map = new HashMap<>();

    /*
     * Double
     */

    public DecimalCommonConfigDto setMaxDoubleValue(double value) {
        return setMaxValue(Double.class, value);
    }

    public DecimalCommonConfigDto setMinDoubleValue(double value) {
        return setMinValue(Double.class, value);
    }

    public DecimalCommonConfigDto setRuleRemarkDouble(IRuleRemark value) {
        return setRuleRemark(Double.class, value);
    }

    /*
     * Float
     */

    public DecimalCommonConfigDto setMaxFloatValue(float value) {
        return setMaxValue(Float.class, value);
    }

    public DecimalCommonConfigDto setMinFloatValue(float value) {
        return setMinValue(Float.class, value);
    }

    public DecimalCommonConfigDto setRuleRemarkFloat(IRuleRemark value) {
        return setRuleRemark(Float.class, value);
    }

    /*
     * BigDecimal
     */

    public DecimalCommonConfigDto setMaxBigDecimalValue(BigDecimal value) {
        return setMaxValue(BigDecimal.class, value);
    }

    public DecimalCommonConfigDto setMinBigDecimalValue(BigDecimal value) {
        return setMinValue(BigDecimal.class, value);
    }

    public DecimalCommonConfigDto setMaxBigDecimalValue(String value) {
        try {
            return setMaxBigDecimalValue(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new DtoGeneratorException("Invalid big integer number: '" + value + "'", e);
        }
    }

    public DecimalCommonConfigDto setMinBigDecimalValue(String value) {
        try {
            return setMinBigDecimalValue(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new DtoGeneratorException("Invalid big integer number: '" + value + "'", e);
        }
    }

    public DecimalCommonConfigDto setRuleRemarkBigDecimal(IRuleRemark value) {
        return setRuleRemark(BigDecimal.class, value);
    }

    /*
     * Common setters
     */

    private DecimalCommonConfigDto setMaxValue(Class<?> type, Number maxValue) {
        map.putIfAbsent(type, new DecimalConfigDto());
        map.get(type).setMaxValue(maxValue);
        return this;
    }

    private DecimalCommonConfigDto setMinValue(Class<?> type, Number minValue) {
        map.putIfAbsent(type, new DecimalConfigDto());
        map.get(type).setMinValue(minValue);
        return this;
    }

    private DecimalCommonConfigDto setRuleRemark(Class<?> type, IRuleRemark ruleRemark) {
        map.putIfAbsent(type, new DecimalConfigDto());
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    DecimalConfigDto getConfigOrNull(Class<? extends Number> generateType) {
        return map.get(generateType);
    }

    public void merge(ConfigDto configDto) {
        throw new NotImplementedException("Not supposed to be in use.");
    }

}
