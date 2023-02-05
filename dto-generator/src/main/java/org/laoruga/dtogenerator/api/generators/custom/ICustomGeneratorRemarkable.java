package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;

import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorRemarkable<T> extends ICustomGenerator<T> {

    void setRuleRemarks(Set<ICustomRuleRemark> ruleRemarks);
}
