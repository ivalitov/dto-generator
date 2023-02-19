package org.laoruga.dtogenerator.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.RuleForCollection;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.api.rules.meta.RulesForCollection;

import java.lang.annotation.Annotation;

import static org.laoruga.dtogenerator.rule.RulesInfoHelper.RuleTypeHelper.*;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RulesInfoHelper {

    public static RuleTypeHelper getHelperType(Annotation ruleAnnotation) {
        if (ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class) != null) {
            return RULE;
        }

        if (ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null) {
            return RULE_FOR_COLLECTION;
        }

        if (ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class) != null) {
            return RULES;
        }

        if (ruleAnnotation.annotationType().getDeclaredAnnotation(RulesForCollection.class) != null) {
            return RULES_FOR_COLLECTION;
        }

        if (ruleAnnotation.annotationType() == CustomRule.class) {
            return CUSTOM_RULE;
        }

        if (ruleAnnotation.annotationType() == NestedDtoRule.class) {
            return NESTED_DTO_RULE;
        }

        return UNKNOWN;

    }

    public enum RuleTypeHelper {
        RULE,
        RULE_FOR_COLLECTION,
        RULES,
        RULES_FOR_COLLECTION,
        CUSTOM_RULE,
        NESTED_DTO_RULE,
        UNKNOWN
    }

}
