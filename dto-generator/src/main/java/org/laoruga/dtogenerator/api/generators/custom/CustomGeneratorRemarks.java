package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorRemarks<T> extends CustomGenerator<T> {

    void setRuleRemark(IRuleRemark ruleRemark);
}
