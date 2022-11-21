package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class LongGenerator implements IGenerator<Long> {

    private final long maxValue;
    private final long minValue;
    private final IRuleRemark ruleRemark;

    @Override
    public Long generate() {
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            return minValue + (long) (Math.random() * (maxValue - minValue));
        }
        if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static LongGeneratorBuilder builder() {
        return new LongGeneratorBuilder();
    }

    public static final class LongGeneratorBuilder implements IGeneratorBuilder<IGenerator<?>> {
        private long maxValue;
        private long minValue;
        private IRuleRemark ruleRemark;

        private LongGeneratorBuilder() {}

        public LongGeneratorBuilder maxValue(long maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public LongGeneratorBuilder minValue(long minValue) {
            this.minValue = minValue;
            return this;
        }

        public LongGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public LongGenerator build() {
            return new LongGenerator(maxValue, minValue, ruleRemark);
        }
    }
}
