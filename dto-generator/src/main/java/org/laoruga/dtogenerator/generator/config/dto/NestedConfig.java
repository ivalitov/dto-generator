package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Builder;
import lombok.Getter;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 27.03.2023
 */
@Builder
@Getter
public class NestedConfig implements ConfigDto {

    private DtoGeneratorBuilder<?> dtoGeneratorBuilder;
    private IRuleRemark ruleRemark;

    @Override
    public void merge(ConfigDto from) {
        NestedConfig nestedConfigFrom = (NestedConfig) from;
        if (nestedConfigFrom.dtoGeneratorBuilder != null) dtoGeneratorBuilder = nestedConfigFrom.dtoGeneratorBuilder;
        if (nestedConfigFrom.ruleRemark != null) ruleRemark = nestedConfigFrom.ruleRemark;
    }

    @Override
    public ConfigDto setRuleRemark(IRuleRemark ruleRemark) {
        this.ruleRemark = ruleRemark;
        return this;
    }

    @Override
    public IRuleRemark getRuleRemark() {
        return ruleRemark;
    }
}
