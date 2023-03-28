package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.configs.DecimalConfigDto;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class DecimalGenerator implements IGenerator<Number> {

    private final Number maxValue;
    private final Number minValue;
    private final int precision;
    private final IRuleRemark ruleRemark;

    public DecimalGenerator(DecimalConfigDto config) {
        maxValue = config.getMaxValue();
        minValue = config.getMinValue();
        precision = config.getPrecision();
        ruleRemark = config.getRuleRemark();
    }

    @Override
    public Number generate() {
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            return RandomUtils.nextNumberDecimal(minValue, maxValue, precision);
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

}
