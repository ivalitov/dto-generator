package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.BooleanRule;

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
public class BooleanConfigDto implements ConfigDto {

    private Double trueProbability;
    private IRuleRemark ruleRemark;

    public BooleanConfigDto(BooleanRule rule) {
        this.trueProbability = rule.trueProbability();
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(ConfigDto from) {
        BooleanConfigDto configDto = (BooleanConfigDto) from;
        if (configDto.getTrueProbability() != null) this.trueProbability = configDto.getTrueProbability();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
