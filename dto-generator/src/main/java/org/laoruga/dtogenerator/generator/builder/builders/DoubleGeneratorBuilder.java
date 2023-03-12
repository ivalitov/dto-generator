package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.NumberDecimalGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.DecimalConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class DoubleGeneratorBuilder implements IGeneratorBuilderConfigurable<Number> {
    private final DecimalConfigDto configDto;

    public DoubleGeneratorBuilder() {
        this.configDto = new DecimalConfigDto();
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

    public NumberDecimalGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        DecimalConfigDto decimalConfigDto = (DecimalConfigDto) configDto;
        return new NumberDecimalGenerator(
                decimalConfigDto.getMaxValue(),
                decimalConfigDto.getMinValue(),
                decimalConfigDto.getPrecision(),
                decimalConfigDto.getRuleRemark());
    }

    public NumberDecimalGenerator build() {
        return build(configDto, false);
    }
}
