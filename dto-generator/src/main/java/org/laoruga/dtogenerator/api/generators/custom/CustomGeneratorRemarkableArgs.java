package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.remarks.CustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorRemarkableArgs<T> extends CustomGenerator<T> {

    void setRuleRemarks(Map<CustomRuleRemark, CustomRuleRemarkArgs> ruleRemarks);
}