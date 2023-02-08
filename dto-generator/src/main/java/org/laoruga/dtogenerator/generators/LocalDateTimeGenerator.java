package org.laoruga.dtogenerator.generators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.LocalDateTimeRule;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.LocalDateTime;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class LocalDateTimeGenerator implements IGenerator<LocalDateTime> {

    private final int leftShiftDays;
    private final int rightShiftDays;
    private final IRuleRemark ruleRemark;

    @Override
    public LocalDateTime generate() {
        LocalDateTime now = LocalDateTime.now();
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return now.minusDays(leftShiftDays);
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return now.plusDays(rightShiftDays);
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            int randomInt = RandomUtils.getRandom().nextInt(leftShiftDays + rightShiftDays + 1);
            LocalDateTime minDate = now.minusDays(leftShiftDays);
            return minDate.plusDays(randomInt);
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static LocalDateTimeGeneratorBuilder builder() {
        return new LocalDateTimeGeneratorBuilder();
    }

    public static final class LocalDateTimeGeneratorBuilder implements IGeneratorBuilderConfigurable {

        private final ConfigDto configDto;

        private LocalDateTimeGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public LocalDateTimeGeneratorBuilder leftShiftDays(int leftShiftDays) {
            configDto.leftShiftDays = leftShiftDays;
            return this;
        }

        public LocalDateTimeGeneratorBuilder rightShiftDays(int rightShiftDays) {
            configDto.rightShiftDays = rightShiftDays;
            return this;
        }

        public LocalDateTimeGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            configDto.ruleRemark = ruleRemark;
            return this;
        }

        public LocalDateTimeGenerator build() {
            return build(configDto, false);
        }

        public LocalDateTimeGenerator build(IConfigDto configDto, boolean merge) {
            if (merge) {
                configDto.merge(this.configDto);
            }
            ConfigDto localDateTimeConfigDto = (ConfigDto) configDto;
            return new LocalDateTimeGenerator(
                    localDateTimeConfigDto.leftShiftDays,
                    localDateTimeConfigDto.rightShiftDays,
                    localDateTimeConfigDto.ruleRemark);
        }
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto {
        private Integer leftShiftDays;
        private Integer rightShiftDays;
        @Setter
        private IRuleRemark ruleRemark;

        public ConfigDto(LocalDateTimeRule rule) {
            this.leftShiftDays = rule.leftShiftDays();
            this.rightShiftDays = rule.rightShiftDays();
            this.ruleRemark = rule.ruleRemark();
        }

        public ConfigDto(){}

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return LocalDateTimeGeneratorBuilder.class;
        }

        public void merge(IConfigDto from) {
            ConfigDto configDto = (ConfigDto) from;
            if (configDto.getLeftShiftDays() != null) this.leftShiftDays = configDto.getLeftShiftDays();
            if (configDto.getRightShiftDays() != null) this.rightShiftDays = configDto.getRightShiftDays();
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        }
    }
}
