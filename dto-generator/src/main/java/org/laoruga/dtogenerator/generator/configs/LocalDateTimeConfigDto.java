package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.LocalDateTimeRule;
import org.laoruga.dtogenerator.generator.builder.builders.LocalDateTimeGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
public class LocalDateTimeConfigDto implements IConfigDto {
    private Integer leftShiftDays;
    private Integer rightShiftDays;
    @Setter
    private IRuleRemark ruleRemark;

    public LocalDateTimeConfigDto(LocalDateTimeRule rule) {
        this.leftShiftDays = rule.leftShiftDays();
        this.rightShiftDays = rule.rightShiftDays();
        this.ruleRemark = rule.ruleRemark();
    }

    public LocalDateTimeConfigDto() {
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return LocalDateTimeGeneratorBuilder.class;
    }

    public void merge(IConfigDto from) {
        LocalDateTimeConfigDto configDto = (LocalDateTimeConfigDto) from;
        if (configDto.getLeftShiftDays() != null) this.leftShiftDays = configDto.getLeftShiftDays();
        if (configDto.getRightShiftDays() != null) this.rightShiftDays = configDto.getRightShiftDays();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
