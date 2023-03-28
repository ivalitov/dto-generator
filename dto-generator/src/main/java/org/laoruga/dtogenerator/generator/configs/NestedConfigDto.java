package org.laoruga.dtogenerator.generator.configs;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 27.03.2023
 */
@Builder
@Getter
public class NestedConfigDto implements ConfigDto {

    private final DtoGenerator<?> dtoGenerator;
    private IRuleRemark ruleRemark;

    @Override
    public void merge(ConfigDto configDto) {
        throw new NotImplementedException();
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
