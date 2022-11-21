package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;


/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Setter(AccessLevel.PACKAGE)
@Getter
class RuleInfoCollection implements IRuleInfo {
    private IRuleInfo collectionRule;
    private IRuleInfo itemRule;
    private String group;

    @Override
    public Annotation getRule() {
        return collectionRule.getRule();
    }

    public boolean isTypesEqual(RuleType type) {
        return collectionRule.isTypesEqual(type);
    }
}
