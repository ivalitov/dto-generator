package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.generator.config.dto.DecimalConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class DecimalNumberGenerator implements Generator<Number> {

    private final Number maxValue;
    private final Number minValue;
    private final int precision;
    private final RuleRemark ruleRemark;

    public DecimalNumberGenerator(DecimalConfig config) {
        maxValue = config.getMaxValue();
        minValue = config.getMinValue();
        precision = config.getPrecision();
        ruleRemark = config.getRuleRemark();
    }

    @Override
    public Number generate() {
        switch ((Boundary) ruleRemark) {

            case MIN_VALUE:
                return minValue;

            case MAX_VALUE:
                return maxValue;

            case NULL_VALUE:
                return null;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                return RandomUtils.nextNumberDecimal(minValue, maxValue, precision);

            default:
                throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
    }

}
