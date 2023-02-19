package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.LongRule;
import org.laoruga.dtogenerator.generator.builder.builders.LongGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Setter
@Getter
@AllArgsConstructor
public class LongConfigDto implements IConfigDto {
    private Long maxValue;
    private Long minValue;
    private IRuleRemark ruleRemark;

    public LongConfigDto(LongRule rule) {
        this.maxValue = rule.maxValue();
        this.minValue = rule.minValue();
        this.ruleRemark = rule.ruleRemark();
    }

    public LongConfigDto() {
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return LongGeneratorBuilder.class;
    }

    public void merge(IConfigDto from) {
        LongConfigDto configDto = (LongConfigDto) from;
        if (configDto.getMaxValue() != null) this.maxValue = configDto.getMaxValue();
        if (configDto.getMinValue() != null) this.minValue = configDto.getMinValue();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
