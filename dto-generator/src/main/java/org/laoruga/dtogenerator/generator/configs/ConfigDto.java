package org.laoruga.dtogenerator.generator.configs;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 02.12.2022
 */
public interface ConfigDto {

    Class<? extends IGeneratorBuilder> getBuilderClass();
    void merge(ConfigDto staticConfig);
    ConfigDto setRuleRemark(IRuleRemark ruleRemark);
    IRuleRemark getRuleRemark();

}
