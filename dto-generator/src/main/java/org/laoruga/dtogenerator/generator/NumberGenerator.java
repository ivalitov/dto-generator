package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.NumberGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.NumberConfigDto;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class NumberGenerator implements IGenerator<Number> {

    private final Number maxValue;
    private final Number minValue;
    private final IRuleRemark ruleRemark;


    @Override
    public Number generate() {

        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            return RandomUtils.nextNumber(minValue, maxValue);
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }

        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static NumberGeneratorBuilder builder() {
        return new NumberGeneratorBuilder();
    }

    public static NumberGeneratorBuilder builder(NumberConfigDto configDto) {
        return new NumberGeneratorBuilder(configDto);
    }

}
