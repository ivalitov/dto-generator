package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.BooleanGenerator;
import org.laoruga.dtogenerator.generator.configs.BooleanConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class BooleanGeneratorBuilder implements IGeneratorBuilderConfigurable<Boolean> {
    private final BooleanConfigDto configDto;

    public BooleanGeneratorBuilder() {
        this.configDto = new BooleanConfigDto();
    }

    public BooleanGeneratorBuilder trueProbability(double trueProbability) {
        configDto.setTrueProbability(trueProbability);
        return this;
    }

    public BooleanGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public BooleanGenerator build() {
        return build(configDto, false);
    }

    public BooleanGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        BooleanConfigDto integerConfigDto = (BooleanConfigDto) configDto;
        return new BooleanGenerator(
                integerConfigDto.getTrueProbability(),
                integerConfigDto.getRuleRemark());
    }
}
