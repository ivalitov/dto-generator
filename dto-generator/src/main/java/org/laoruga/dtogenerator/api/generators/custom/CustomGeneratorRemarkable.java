package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.remarks.CustomRuleRemark;

import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorRemarkable<T> extends CustomGenerator<T> {

    void setRuleRemarks(Set<CustomRuleRemark> ruleRemarks);
}
