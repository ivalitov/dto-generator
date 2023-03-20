package org.laoruga.dtogenerator.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;

import static org.laoruga.dtogenerator.rule.RulesInfoHelper.RuleTypeHelper.*;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RulesInfoHelper {

    public static RuleTypeHelper getHelperType(Annotation ruleAnnotation) {

        if (ruleAnnotation.annotationType() == CustomRule.class) {
            return CUSTOM_RULE;
        }

        if (ruleAnnotation.annotationType() == NestedDtoRule.class) {
            return NESTED_DTO_RULE;
        }

        Rule rule = ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class);

        if (rule != null) {

            if (rule.value() == RuleType.COLLECTION) {
                return RULE_FOR_COLLECTION;
            }

            if (rule.value() == RuleType.MAP) {
                return RULE_FOR_MAP;
            }

            return RULE;
        }

        Rules rules = ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class);

        if (rules != null) {

            if (rules.value() == RuleType.COLLECTION) {
                return RULES_FOR_COLLECTION;
            }

            if (rules.value() == RuleType.MAP) {
                return RULES_FOR_MAP;
            }

            return RULES;
        }

        return UNKNOWN;
    }

    public enum RuleTypeHelper {
        RULE,
        RULE_FOR_MAP,
        RULE_FOR_COLLECTION,
        RULES,
        RULES_FOR_COLLECTION,
        RULES_FOR_MAP,
        CUSTOM_RULE,
        NESTED_DTO_RULE,
        UNKNOWN
    }

}
