package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.IntegerGenerator;
import org.laoruga.dtogenerator.generator.configs.IConfigDto;
import org.laoruga.dtogenerator.generator.configs.IntegerConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class IntegerGeneratorBuilder implements IGeneratorBuilderConfigurable {
    private final IntegerConfigDto configDto;

    public IntegerGeneratorBuilder() {
        this.configDto = new IntegerConfigDto();
    }

    public IntegerGeneratorBuilder maxValue(int maxValue) {
        configDto.setMaxValue(maxValue);
        return this;
    }

    public IntegerGeneratorBuilder minValue(int minValue) {
        configDto.setMinValue(minValue);
        return this;
    }

    public IntegerGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public IntegerGenerator build() {
        return build(configDto, false);
    }

    public IntegerGenerator build(IConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        IntegerConfigDto integerConfigDto = (IntegerConfigDto) configDto;
        return new IntegerGenerator(
                integerConfigDto.getMaxValue(),
                integerConfigDto.getMinValue(),
                integerConfigDto.getRuleRemark());
    }
}
