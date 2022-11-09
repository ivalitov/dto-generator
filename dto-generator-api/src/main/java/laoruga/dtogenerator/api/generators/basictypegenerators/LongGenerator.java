package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.AllArgsConstructor;

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
