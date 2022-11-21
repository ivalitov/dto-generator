package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class IntegerGenerator implements IGenerator<Integer> {

    private final int maxValue;
    private final int minValue;
    private final IRuleRemark ruleRemark;

    @Override
    public Integer generate() {
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            return RandomUtils.nextInt(minValue, maxValue);
        }
        if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static IntegerGeneratorBuilder builder() {
        return new IntegerGeneratorBuilder();
    }

    public static final class IntegerGeneratorBuilder implements IGeneratorBuilder<IGenerator<?>> {
        private int maxValue = IntegerRule.DEFAULT_MAX;
        private int minValue = IntegerRule.DEFAULT_MIN;
        private IRuleRemark ruleRemark = IntegerRule.DEFAULT_RULE_REMARK;

        private IntegerGeneratorBuilder() {}

        public IntegerGeneratorBuilder maxValue(int maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public IntegerGeneratorBuilder minValue(int minValue) {
            this.minValue = minValue;
            return this;
        }

        public IntegerGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public IntegerGenerator build() {
            return new IntegerGenerator(maxValue, minValue, ruleRemark);
        }
    }
}
