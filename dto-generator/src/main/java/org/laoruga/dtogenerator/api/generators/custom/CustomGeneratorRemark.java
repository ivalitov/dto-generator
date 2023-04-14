package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.RuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorRemark<T> extends CustomGenerator<T> {

    void setRuleRemark(RuleRemark ruleRemark);
}
