package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.DoubleRules;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Precision;

import java.util.Random;

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
            double generated = minValue + new Random().nextDouble() * (maxValue - minValue);
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

    public static final class DoubleGeneratorBuilder implements IGeneratorBuilder {
        private double maxValue = DoubleRules.DEFAULT_MAX;
        private double minValue = DoubleRules.DEFAULT_MIN;
        private int precision = DoubleRules.DEFAULT_PRECISION;
        private IRuleRemark ruleRemark = DoubleRules.DEFAULT_RULE_REMARK;

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
