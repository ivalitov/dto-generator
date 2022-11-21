package org.laoruga.dtogenerator;

import java.lang.annotation.Annotation;

import static org.laoruga.dtogenerator.RulesInfoHelper.*;

/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
enum RuleType {
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
