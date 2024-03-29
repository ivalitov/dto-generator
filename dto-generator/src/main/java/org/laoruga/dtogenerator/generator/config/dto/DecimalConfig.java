package org.laoruga.dtogenerator.generator.config.dto;

import com.google.common.primitives.Primitives;
import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.rules.DecimalRule;

import java.math.BigDecimal;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DecimalConfig implements ConfigDto {
    private Number maxValue;
    private Number minValue;
    private Integer precision;
    private RuleRemark ruleRemark;
    private Class<? extends Number> fieldType;

    public DecimalConfig(DecimalRule rule, Class<? extends Number> fieldType) {
        fieldType = Primitives.wrap(fieldType);

        this.fieldType = fieldType;
        this.ruleRemark = rule.boundary();
        this.precision = rule.precision();

        if (fieldType == Double.class) {
            this.maxValue = rule.maxDouble();
            this.minValue = rule.minDouble();
        } else if (fieldType == Float.class) {
            this.maxValue = rule.maxFloat();
            this.minValue = rule.minFloat();
        } else if (fieldType == BigDecimal.class) {
            this.maxValue = new BigDecimal(rule.maxBigDecimal());
            this.minValue = new BigDecimal(rule.minBigDecimal());
        } else {
            throw new IllegalStateException("Unexpected field type: '" + fieldType + "'");
        }
    }

    /**
     * Builder Class.
     * Types of min and max values have to be the same as generated type, this config intended for.
     */
    public static class DecimalConfigBuilder {

        public DecimalConfigBuilder maxValue(Double maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public DecimalConfigBuilder minValue(Double minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        public DecimalConfigBuilder maxValue(Float maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public DecimalConfigBuilder minValue(Float minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        public DecimalConfigBuilder maxValue(BigDecimal maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public DecimalConfigBuilder minValue(BigDecimal minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        private void check(Number valueToSet, Number another) {
            if (another != null && another.getClass() != valueToSet.getClass()) {
                throw new IllegalArgumentException("Wrong bound type: '" + valueToSet + "'. " +
                        "Bound with type '" + another + "' expexted.");
            }
        }
    }

    public void merge(ConfigDto configDto) {

        boolean commonConfig = configDto.getClass() == DecimalCommonConfig.class;

        DecimalConfig configFrom = commonConfig
                ? ((DecimalCommonConfig) configDto).getConfigOrNull(fieldType)
                : (DecimalConfig) configDto;

        if (commonConfig) {
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        }

        if (configFrom != null) {
            if (configFrom.getMaxValue() != null) this.maxValue = configFrom.getMaxValue();
            if (configFrom.getMinValue() != null) this.minValue = configFrom.getMinValue();
            if (configFrom.getPrecision() != null) this.precision = configFrom.getPrecision();
            if (configFrom.getRuleRemark() != null) this.ruleRemark = configFrom.getRuleRemark();
        }
    }

}
