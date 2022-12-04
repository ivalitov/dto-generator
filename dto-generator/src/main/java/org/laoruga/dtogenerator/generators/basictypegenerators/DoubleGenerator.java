package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Precision;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.DoubleRule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

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

    public static final class DoubleGeneratorBuilder implements IGeneratorBuilderConfigurable {
        private final ConfigDto configDto;

        private DoubleGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public DoubleGeneratorBuilder maxValue(double maxValue) {
            configDto.maxValue = maxValue;
            return this;
        }

        public DoubleGeneratorBuilder minValue(double minValue) {
            configDto.minValue = minValue;
            return this;
        }

        public DoubleGeneratorBuilder precision(int precision) {
            configDto.precision = precision;
            return this;
        }

        public DoubleGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            configDto.ruleRemark = ruleRemark;
            return this;
        }

        public DoubleGenerator build(IConfigDto configDto, boolean merge) {
            if (merge) {
                configDto.merge(this.configDto);
            }
            DoubleGenerator.ConfigDto doubleConfigDto = (DoubleGenerator.ConfigDto) configDto;
            return new DoubleGenerator(
                    doubleConfigDto.maxValue,
                    doubleConfigDto.minValue,
                    doubleConfigDto.precision,
                    doubleConfigDto.ruleRemark);
        }

        public DoubleGenerator build() {
            return build(configDto, false);
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto {
        private Double maxValue;
        private Double minValue;
        private Integer precision;
        @Setter
        private IRuleRemark ruleRemark;

        public ConfigDto() {}

        public ConfigDto(DoubleRule rule) {
            this.maxValue = rule.maxValue();
            this.minValue = rule.minValue();
            this.precision = rule.precision();
            this.ruleRemark = rule.ruleRemark();
        }

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return DoubleGeneratorBuilder.class;
        }

        public void merge(IConfigDto from) {
            DoubleGenerator.ConfigDto fromConfigDto = (DoubleGenerator.ConfigDto) from;
            if (fromConfigDto.getMaxValue() != null) this.maxValue = fromConfigDto.getMaxValue();
            if (fromConfigDto.getMinValue() != null) this.minValue = fromConfigDto.getMinValue();
            if (fromConfigDto.getPrecision() != null) this.precision = fromConfigDto.getPrecision();
            if (fromConfigDto.getRuleRemark() != null) this.ruleRemark = fromConfigDto.getRuleRemark();
        }
    }
}
