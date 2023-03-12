package org.laoruga.dtogenerator.generator.configs;

import com.google.common.primitives.Primitives;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.DecimalRule;

import java.math.BigDecimal;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DecimalConfigDto implements ConfigDto {
    private Number maxValue;
    private Number minValue;
    private Integer precision;
    private IRuleRemark ruleRemark;
    private Class<? extends Number> fieldType;

    public DecimalConfigDto(DecimalRule rule, Class<? extends Number> fieldType) {
        fieldType = Primitives.wrap(fieldType);

        this.fieldType = fieldType;
        this.ruleRemark = rule.ruleRemark();
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

    public void merge(ConfigDto configDto) {
        DecimalConfigDto fromConfigDto = (DecimalConfigDto) configDto;
        if (fromConfigDto.getMaxValue() != null) this.maxValue = fromConfigDto.getMaxValue();
        if (fromConfigDto.getMinValue() != null) this.minValue = fromConfigDto.getMinValue();
        if (fromConfigDto.getPrecision() != null) this.precision = fromConfigDto.getPrecision();
        if (fromConfigDto.getRuleRemark() != null) this.ruleRemark = fromConfigDto.getRuleRemark();
    }
}
