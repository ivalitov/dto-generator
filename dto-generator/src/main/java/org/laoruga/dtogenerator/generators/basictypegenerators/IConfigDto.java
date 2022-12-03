package org.laoruga.dtogenerator.generators.basictypegenerators;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 02.12.2022
 */
public interface IConfigDto {
    void merge(IConfigDto staticConfig);

    void setRuleRemark(IRuleRemark ruleRemark);
    IRuleRemark getRuleRemark();


}
