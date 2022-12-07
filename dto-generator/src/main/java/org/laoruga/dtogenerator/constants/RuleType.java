package org.laoruga.dtogenerator.constants;

import java.lang.annotation.Annotation;

import static org.laoruga.dtogenerator.rules.RulesInfoHelper.*;

/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
public enum RuleType {
    BASIC,
    CUSTOM,
    NESTED,
    COLLECTION;

    public static RuleType getType(Annotation rule) {
        if (isItCustomRule(rule)) {
            return CUSTOM;
        } else if (isItNestedRule(rule)) {
            return NESTED;
        } else if (isItCollectionRule(rule)) {
            return COLLECTION;
        } else {
            return BASIC;
        }
    }

}
