package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.DoubleRule;
import org.laoruga.dtogenerator.generator.builder.builders.DoubleGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class DoubleConfigDto implements IConfigDto {
    private Double maxValue;
    private Double minValue;
    private Integer precision;
    private IRuleRemark ruleRemark;

    public DoubleConfigDto() {
    }

    public DoubleConfigDto(DoubleRule rule) {
        this.maxValue = rule.maxValue();
        this.minValue = rule.minValue();
        this.precision = rule.precision();
        this.ruleRemark = rule.ruleRemark();
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return DoubleGeneratorBuilder.class;
    }

    public void merge(IConfigDto from) {
        DoubleConfigDto fromConfigDto = (DoubleConfigDto) from;
        if (fromConfigDto.getMaxValue() != null) this.maxValue = fromConfigDto.getMaxValue();
        if (fromConfigDto.getMinValue() != null) this.minValue = fromConfigDto.getMinValue();
        if (fromConfigDto.getPrecision() != null) this.precision = fromConfigDto.getPrecision();
        if (fromConfigDto.getRuleRemark() != null) this.ruleRemark = fromConfigDto.getRuleRemark();
    }
}
