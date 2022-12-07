package org.laoruga.dtogenerator.rules;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;

/**
 * @author Il'dar Valitov
 * Created on 10.11.2022
 */
@ToString
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PUBLIC)
class RuleInfo implements IRuleInfo {
    private Annotation rule;
    private RuleType ruleType;
    private boolean multipleRules;
    private String group;

    public static RuleInfoBuilder builder(){
        return new RuleInfoBuilder();
    }
    public boolean isTypesEqual(RuleType type) {
        return ruleType == type;
    }

}
