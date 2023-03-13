package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.NumberGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.NumberConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class NumberGeneratorBuilder implements IGeneratorBuilderConfigurable<Number> {
    private final NumberConfigDto configDto;

    public NumberGeneratorBuilder() {
        this.configDto = new NumberConfigDto();
    }

    public NumberGeneratorBuilder(NumberConfigDto configDto) {
        this.configDto = configDto;
    }

    public NumberGeneratorBuilder setMaxValue(Number maxValue) {
        configDto.setMaxValue(maxValue);
        return this;
    }

    public NumberGeneratorBuilder setMinValue(Number minValue) {
        configDto.setMinValue(minValue);
        return this;
    }

    public NumberGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public NumberGenerator build() {
        return build(configDto, false);
    }

    public NumberGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        NumberConfigDto integerConfigDto;
        try {
            integerConfigDto = (NumberConfigDto) configDto;
        } catch (ClassCastException e) {
            throw e;
        }
        return new NumberGenerator(
                integerConfigDto.getMaxValue(),
                integerConfigDto.getMinValue(),
                integerConfigDto.isAtomic(),
                integerConfigDto.getRuleRemark()
        );
    }
}
