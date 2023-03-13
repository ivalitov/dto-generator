package org.laoruga.dtogenerator.generator.configs;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 02.12.2022
 */
public interface ConfigDto {

    /**
     * @param configDto - non-null values from that object replaces current (this) values
     */
    void merge(ConfigDto configDto);
    ConfigDto setRuleRemark(IRuleRemark ruleRemark);
    IRuleRemark getRuleRemark();

}
