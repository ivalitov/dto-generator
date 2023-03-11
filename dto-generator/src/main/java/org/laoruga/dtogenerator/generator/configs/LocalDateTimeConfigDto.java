package org.laoruga.dtogenerator.generator.configs;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.LocalDateTimeRule;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class LocalDateTimeConfigDto implements ConfigDto {
    private Integer leftShiftDays;
    private Integer rightShiftDays;
    private IRuleRemark ruleRemark;

    public LocalDateTimeConfigDto(LocalDateTimeRule rule) {
        this.leftShiftDays = rule.leftShiftDays();
        this.rightShiftDays = rule.rightShiftDays();
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(ConfigDto from) {
        LocalDateTimeConfigDto configDto = (LocalDateTimeConfigDto) from;
        if (configDto.getLeftShiftDays() != null) this.leftShiftDays = configDto.getLeftShiftDays();
        if (configDto.getRightShiftDays() != null) this.rightShiftDays = configDto.getRightShiftDays();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
