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
        switch ((RuleRemark) ruleRemark) {

            case MIN_VALUE:
                return false;

            case MAX_VALUE:
                return true;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                return RandomUtils.RANDOM.nextDouble() < trueProbability;

            case NULL_VALUE:
                return null;

            default:
                throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
    }

}
