package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.RuleRemark;
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
public class BooleanConfig implements ConfigDto {

    private Double trueProbability;
    private RuleRemark ruleRemark;

    public BooleanConfig(BooleanRule rule) {
        this.trueProbability = rule.trueProbability();
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(ConfigDto from) {
        BooleanConfig configDto = (BooleanConfig) from;
        if (configDto.getTrueProbability() != null) this.trueProbability = configDto.getTrueProbability();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
