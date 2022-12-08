package org.laoruga.dtogenerator.typegenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.constants.RuleRemark;
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
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            return RandomUtils.nextInt(minValue, maxValue);
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static IntegerGeneratorBuilder builder() {
        return new IntegerGeneratorBuilder();
    }

    public static final class IntegerGeneratorBuilder implements IGeneratorBuilderConfigurable {
        private final ConfigDto configDto;

        private IntegerGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public IntegerGeneratorBuilder maxValue(int maxValue) {
            configDto.maxValue = maxValue;
            return this;
        }

        public IntegerGeneratorBuilder minValue(int minValue) {
            configDto.minValue = minValue;
            return this;
        }

        public IntegerGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            configDto.ruleRemark = ruleRemark;
            return this;
        }

        public IntegerGenerator build() {
            return build(configDto, false);
        }

        public IntegerGenerator build(IConfigDto configDto, boolean merge) {
            if (merge) {
                configDto.merge(this.configDto);
            }
            ConfigDto integerConfigDto = (ConfigDto) configDto;
            return new IntegerGenerator(
                    integerConfigDto.maxValue,
                    integerConfigDto.minValue,
                    integerConfigDto.ruleRemark);
        }
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto {

        private Integer maxValue;
        private Integer minValue;
        private IRuleRemark ruleRemark;

        public ConfigDto(IntegerRule rule) {
            this.maxValue = rule.maxValue();
            this.minValue = rule.minValue();
            this.ruleRemark = rule.ruleRemark();
        }

        public ConfigDto() {}

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return IntegerGeneratorBuilder.class;
        }

        public void merge(IConfigDto from) {
            ConfigDto configDto = (ConfigDto) from;
            if (configDto.getMaxValue() != null) this.maxValue = configDto.getMaxValue();
            if (configDto.getMinValue() != null) this.minValue = configDto.getMinValue();
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        }
    }
}
