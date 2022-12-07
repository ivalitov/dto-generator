package org.laoruga.dtogenerator.rules;

import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
public interface IRuleInfo {
    Annotation getRule();
    String getGroup();
    boolean isTypesEqual(RuleType ruleType);
}
