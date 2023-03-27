package org.laoruga.dtogenerator.constants;

import org.laoruga.dtogenerator.rule.RulesInfoHelper;

import java.lang.annotation.Annotation;


/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
public enum RuleType {
    BASIC,
    CUSTOM,
    NESTED,
    COLLECTION,
    ARRAY,
    MAP;

    public static RuleType getType(Annotation rule) {
        switch (RulesInfoHelper.getHelperType(rule)) {
            case RULE:
            case RULES:
                return BASIC;

            case RULE_FOR_COLLECTION:
            case RULES_FOR_COLLECTION:
                return COLLECTION;

            case RULE_FOR_ARRAY:
                return ARRAY;

            case RULE_FOR_MAP:
            case RULES_FOR_MAP:
                return MAP;

            case CUSTOM_RULE:
                return CUSTOM;

            case NESTED_DTO_RULE:
                return NESTED;
        }
        throw new IllegalArgumentException("Unexpected annotation: " + rule);
    }

}
