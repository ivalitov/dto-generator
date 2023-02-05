package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemarkArgs;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorRemarkableArgs<T> extends ICustomGenerator<T> {

    void setRuleRemarks(Map<ICustomRuleRemark, ICustomRuleRemarkArgs> ruleRemarks);
}