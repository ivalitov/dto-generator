package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.DoubleRule;
import laoruga.dtogenerator.api.util.RandomUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Precision;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class DoubleGenerator implements IGenerator<Double> {

    private final double maxValue;
    private final double minValue;
    private final int precision;
    private final IRuleRemark ruleRemark;

    @Override
    public Double generate() {
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            double generated = minValue + RandomUtils.getRandom().nextDouble() * (maxValue - minValue);
            return Precision.round(generated, precision);
        }
        if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static DoubleGeneratorBuilder builder() {
        return new DoubleGeneratorBuilder();
    }

    public static final class DoubleGeneratorBuilder implements IGeneratorBuilder<IGenerator<?>> {
        private double maxValue = DoubleRule.DEFAULT_MAX;
        private double minValue = DoubleRule.DEFAULT_MIN;
        private int precision = DoubleRule.DEFAULT_PRECISION;
        private IRuleRemark ruleRemark = DoubleRule.DEFAULT_RULE_REMARK;

        private DoubleGeneratorBuilder() {}

        public DoubleGeneratorBuilder maxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public DoubleGeneratorBuilder minValue(double minValue) {
            this.minValue = minValue;
            return this;
        }

        public DoubleGeneratorBuilder precision(int precision) {
            this.precision = precision;
            return this;
        }

        public DoubleGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public DoubleGenerator build() {
            return new DoubleGenerator(maxValue, minValue, precision, ruleRemark);
        }
    }
}
