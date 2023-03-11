package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.DoubleGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.DoubleConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class DoubleGeneratorBuilder implements IGeneratorBuilderConfigurable<Double> {
    private final DoubleConfigDto configDto;

    public DoubleGeneratorBuilder() {
        this.configDto = new DoubleConfigDto();
    }

    public DoubleGeneratorBuilder maxValue(double maxValue) {
        configDto.setMaxValue(maxValue);
        return this;
    }

    public DoubleGeneratorBuilder minValue(double minValue) {
        configDto.setMinValue(minValue);
        return this;
    }

    public DoubleGeneratorBuilder precision(int precision) {
        configDto.setPrecision(precision);
        return this;
    }

    public DoubleGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public DoubleGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        DoubleConfigDto doubleConfigDto = (DoubleConfigDto) configDto;
        return new DoubleGenerator(
                doubleConfigDto.getMaxValue(),
                doubleConfigDto.getMinValue(),
                doubleConfigDto.getPrecision(),
                doubleConfigDto.getRuleRemark());
    }

    public DoubleGenerator build() {
        return build(configDto, false);
    }
}
