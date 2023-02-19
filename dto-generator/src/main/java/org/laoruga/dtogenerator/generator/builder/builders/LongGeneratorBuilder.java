package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.LongGenerator;
import org.laoruga.dtogenerator.generator.configs.IConfigDto;
import org.laoruga.dtogenerator.generator.configs.LongConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class LongGeneratorBuilder implements IGeneratorBuilderConfigurable {

    private final LongConfigDto configDto;

    public LongGeneratorBuilder() {
        this.configDto = new LongConfigDto();
    }

    public LongGeneratorBuilder maxValue(long maxValue) {
        configDto.setMaxValue(maxValue);;
        return this;
    }

    public LongGeneratorBuilder minValue(long minValue) {
        configDto.setMinValue(minValue);
        return this;
    }

    public LongGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public LongGenerator build() {
        return build(configDto, false);
    }


    public LongGenerator build(IConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        LongConfigDto longConfigDto = (LongConfigDto) configDto;
        return new LongGenerator(
                longConfigDto.getMaxValue(),
                longConfigDto.getMinValue(),
                longConfigDto.getRuleRemark());
    }
}
