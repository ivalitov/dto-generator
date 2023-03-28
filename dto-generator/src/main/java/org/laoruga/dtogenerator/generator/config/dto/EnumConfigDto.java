package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.EnumRule;

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
public class EnumConfigDto implements ConfigDto {
    private String[] possibleEnumNames;
    private Class<? extends Enum<?>> enumClass;
    private IRuleRemark ruleRemark;

    public EnumConfigDto(EnumRule enumRule) {
        possibleEnumNames = enumRule.possibleEnumNames();
        ruleRemark = enumRule.ruleRemark();
    }

    public void merge(ConfigDto from) {
        EnumConfigDto configDto = (EnumConfigDto) from;
        if (configDto.getPossibleEnumNames() != null) this.possibleEnumNames = configDto.getPossibleEnumNames();
        if (configDto.getEnumClass() != null) this.enumClass = configDto.getEnumClass();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
