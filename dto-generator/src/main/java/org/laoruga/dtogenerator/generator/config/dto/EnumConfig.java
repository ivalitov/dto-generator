package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.RuleRemark;
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
public class EnumConfig implements ConfigDto {
    private String[] possibleEnumNames;
    private Class<? extends Enum<?>> enumClass;
    private RuleRemark ruleRemark;

    public EnumConfig(EnumRule enumRule) {
        possibleEnumNames = enumRule.possibleEnumNames();
        ruleRemark = enumRule.ruleRemark();
    }

    public void merge(ConfigDto from) {
        EnumConfig configDto = (EnumConfig) from;
        if (configDto.getPossibleEnumNames() != null) this.possibleEnumNames = configDto.getPossibleEnumNames();
        if (configDto.getEnumClass() != null) this.enumClass = configDto.getEnumClass();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
