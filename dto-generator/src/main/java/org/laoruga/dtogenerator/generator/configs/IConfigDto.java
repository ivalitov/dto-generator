package org.laoruga.dtogenerator.generator.configs;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 02.12.2022
 */
public interface IConfigDto {

    Class<? extends IGeneratorBuilder> getBuilderClass();
    void merge(IConfigDto staticConfig);
    void setRuleRemark(IRuleRemark ruleRemark);
    IRuleRemark getRuleRemark();


}
