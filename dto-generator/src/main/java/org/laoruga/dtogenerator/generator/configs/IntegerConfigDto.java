package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.generator.builder.builders.IntegerGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
public class IntegerConfigDto implements IConfigDto {

    private Integer maxValue;
    private Integer minValue;
    private IRuleRemark ruleRemark;

    public IntegerConfigDto(IntegerRule rule) {
        this.maxValue = rule.maxValue();
        this.minValue = rule.minValue();
        this.ruleRemark = rule.ruleRemark();
    }

    public IntegerConfigDto() {
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return IntegerGeneratorBuilder.class;
    }

    public void merge(IConfigDto from) {
        IntegerConfigDto configDto = (IntegerConfigDto) from;
        if (configDto.getMaxValue() != null) this.maxValue = configDto.getMaxValue();
        if (configDto.getMinValue() != null) this.minValue = configDto.getMinValue();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
