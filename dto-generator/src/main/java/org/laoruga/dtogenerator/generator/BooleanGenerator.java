package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.config.dto.BooleanConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class BooleanGenerator implements IGenerator<Boolean> {

    private final double trueProbability;
    private final IRuleRemark ruleRemark;

    public BooleanGenerator(BooleanConfig configDto) {
        trueProbability = configDto.getTrueProbability();
        ruleRemark = configDto.getRuleRemark();
    }

    @Override
    public Boolean generate() {
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return false;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return true;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            return RandomUtils.RANDOM.nextDouble() < trueProbability;
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

}
