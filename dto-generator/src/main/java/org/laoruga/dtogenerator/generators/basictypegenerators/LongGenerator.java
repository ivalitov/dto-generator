package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.LongRule;
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

    public static final class LongGeneratorBuilder implements IGeneratorBuilderConfigurable {

        private final ConfigDto configDto;

        private LongGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public LongGeneratorBuilder maxValue(long maxValue) {
            configDto.maxValue = maxValue;
            return this;
        }

        public LongGeneratorBuilder minValue(long minValue) {
            configDto.minValue = minValue;
            return this;
        }

        public LongGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            configDto.ruleRemark = ruleRemark;
            return this;
        }

        public LongGenerator build() {
            return build(configDto, false);
        }


        public LongGenerator build(IConfigDto configDto, boolean merge) {
            if (merge) {
                configDto.merge(this.configDto);
            }
            ConfigDto longConfigDto = (ConfigDto) configDto;
            return new LongGenerator(
                    longConfigDto.maxValue,
                    longConfigDto.minValue,
                    longConfigDto.ruleRemark);
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto{
        private Long maxValue;
        private Long minValue;
        @Setter
        private IRuleRemark ruleRemark;

        public ConfigDto(LongRule rule) {
            this.maxValue = rule.maxValue();
            this.minValue = rule.minValue();
            this.ruleRemark = rule.ruleRemark();
        }

        public ConfigDto() { }

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return LongGeneratorBuilder.class;
        }

        public void merge(IConfigDto from) {
            ConfigDto configDto = (ConfigDto) from;
            if (configDto.getMaxValue() != null) this.maxValue = configDto.getMaxValue();
            if (configDto.getMinValue() != null) this.minValue = configDto.getMinValue();
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        }
    }
}
