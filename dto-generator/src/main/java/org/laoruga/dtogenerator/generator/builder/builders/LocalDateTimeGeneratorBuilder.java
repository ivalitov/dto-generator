package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.LocalDateTimeGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.LocalDateTimeConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class LocalDateTimeGeneratorBuilder implements IGeneratorBuilderConfigurable {

    private final LocalDateTimeConfigDto configDto;

    public LocalDateTimeGeneratorBuilder() {
        this.configDto = new LocalDateTimeConfigDto();
    }

    public LocalDateTimeGeneratorBuilder leftShiftDays(int leftShiftDays) {
        configDto.setLeftShiftDays(leftShiftDays);
        return this;
    }

    public LocalDateTimeGeneratorBuilder rightShiftDays(int rightShiftDays) {
        configDto.setRightShiftDays(rightShiftDays);
        return this;
    }

    public LocalDateTimeGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public LocalDateTimeGenerator build() {
        return build(configDto, false);
    }

    public LocalDateTimeGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        LocalDateTimeConfigDto localDateTimeConfigDto = (LocalDateTimeConfigDto) configDto;
        return new LocalDateTimeGenerator(
                localDateTimeConfigDto.getLeftShiftDays(),
                localDateTimeConfigDto.getRightShiftDays(),
                localDateTimeConfigDto.getRuleRemark());
    }
}
