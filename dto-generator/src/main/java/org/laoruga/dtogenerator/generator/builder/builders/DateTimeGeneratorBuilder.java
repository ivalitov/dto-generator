package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.DateTimeGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;

import java.time.temporal.Temporal;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class DateTimeGeneratorBuilder implements IGeneratorBuilderConfigurable<Temporal> {

    private final DateTimeConfigDto configDto;

    public DateTimeGeneratorBuilder() {
        this.configDto = new DateTimeConfigDto();
    }

    public DateTimeGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public DateTimeGenerator build() {
        return build(configDto, false);
    }

    public DateTimeGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        DateTimeConfigDto dateTimeConfigDto = (DateTimeConfigDto) configDto;
        return new DateTimeGenerator(
                dateTimeConfigDto.getChronoUnitConfigList(),
                dateTimeConfigDto.getRuleRemark(),
                dateTimeConfigDto.getGeneratedType());
    }
}
