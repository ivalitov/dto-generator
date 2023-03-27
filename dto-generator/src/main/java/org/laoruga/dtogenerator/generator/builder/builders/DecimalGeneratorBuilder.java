package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.DecimalGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.DecimalConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class DecimalGeneratorBuilder implements IGeneratorBuilderConfigurable<Number> {
    private final DecimalConfigDto configDto;

    public DecimalGeneratorBuilder() {
        this.configDto = new DecimalConfigDto();
    }

    public DecimalGeneratorBuilder maxValue(double maxValue) {
        configDto.setMaxValue(maxValue);
        return this;
    }

    public DecimalGeneratorBuilder minValue(double minValue) {
        configDto.setMinValue(minValue);
        return this;
    }

    public DecimalGeneratorBuilder precision(int precision) {
        configDto.setPrecision(precision);
        return this;
    }

    public DecimalGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public DecimalGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        DecimalConfigDto decimalConfigDto = (DecimalConfigDto) configDto;
        return new DecimalGenerator(
                decimalConfigDto.getMaxValue(),
                decimalConfigDto.getMinValue(),
                decimalConfigDto.getPrecision(),
                decimalConfigDto.getRuleRemark());
    }

    public DecimalGenerator build() {
        return build(configDto, false);
    }
}
