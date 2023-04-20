package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@NoArgsConstructor
public class DecimalCommonConfig implements ConfigDto {

    @Getter
    @Setter
    @Accessors(chain = true)
    private RuleRemark ruleRemark;

    private final Map<Class<?>, DecimalConfig> map = new HashMap<>();

    /*
     * Double
     */

    public DecimalCommonConfig setMaxDoubleValue(double value) {
        return setMaxValue(Double.class, value);
    }

    public DecimalCommonConfig setMinDoubleValue(double value) {
        return setMinValue(Double.class, value);
    }

    public DecimalCommonConfig setRuleRemarkDouble(RuleRemark value) {
        return setRuleRemark(Double.class, value);
    }

    public DecimalCommonConfig setPrecisionDouble(int value) {
        return setPrecision(Double.class, value);
    }

    /*
     * Float
     */

    public DecimalCommonConfig setMaxFloatValue(float value) {
        return setMaxValue(Float.class, value);
    }

    public DecimalCommonConfig setMinFloatValue(float value) {
        return setMinValue(Float.class, value);
    }

    public DecimalCommonConfig setRuleRemarkFloat(RuleRemark value) {
        return setRuleRemark(Float.class, value);
    }

    public DecimalCommonConfig setPrecisionFloat(int value) {
        return setPrecision(Float.class, value);
    }

    /*
     * BigDecimal
     */

    public DecimalCommonConfig setMaxBigDecimalValue(BigDecimal value) {
        return setMaxValue(BigDecimal.class, value);
    }

    public DecimalCommonConfig setMinBigDecimalValue(BigDecimal value) {
        return setMinValue(BigDecimal.class, value);
    }

    public DecimalCommonConfig setMaxBigDecimalValue(String value) {
        try {
            return setMaxBigDecimalValue(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new DtoGeneratorException("Invalid big integer number: '" + value + "'", e);
        }
    }

    public DecimalCommonConfig setMinBigDecimalValue(String value) {
        try {
            return setMinBigDecimalValue(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new DtoGeneratorException("Invalid big integer number: '" + value + "'", e);
        }
    }

    public DecimalCommonConfig setRuleRemarkBigDecimal(RuleRemark value) {
        return setRuleRemark(BigDecimal.class, value);
    }

    public DecimalCommonConfig setPrecisionBigDecimal(int value) {
        return setPrecision(BigDecimal.class, value);
    }

    /*
     * Common setters
     */

    private DecimalCommonConfig setMaxValue(Class<?> type, Number maxValue) {
        map.putIfAbsent(type, new DecimalConfig());
        map.get(type).setMaxValue(maxValue);
        return this;
    }

    private DecimalCommonConfig setMinValue(Class<?> type, Number minValue) {
        map.putIfAbsent(type, new DecimalConfig());
        map.get(type).setMinValue(minValue);
        return this;
    }

    private DecimalCommonConfig setRuleRemark(Class<?> type, RuleRemark ruleRemark) {
        map.putIfAbsent(type, new DecimalConfig());
        map.get(type).setRuleRemark(ruleRemark);
        return this;
    }

    private DecimalCommonConfig setPrecision(Class<?> type, int precision) {
        map.putIfAbsent(type, new DecimalConfig());
        map.get(type).setPrecision(precision);
        return this;
    }

    DecimalConfig getConfigOrNull(Class<? extends Number> generateType) {
        return map.get(generateType);
    }

    public void merge(ConfigDto configDto) {
        throw new NotImplementedException("Not supposed to be in use.");
    }

}
