package org.laoruga.dtogenerator.rule;

import lombok.*;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;

/**
 * @author Il'dar Valitov
 * Created on 10.11.2022
 */
@ToString
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PUBLIC)
@Builder
class RuleInfo implements IRuleInfo {
    private Annotation rule;
    private RuleType ruleType;
    private boolean multipleRules;
    private String group;

    public boolean isTypesEqual(RuleType type) {
        return ruleType == type;
    }

}
