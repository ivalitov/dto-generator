package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.RuleForCollection;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.api.rules.meta.RulesForCollection;

import java.lang.annotation.Annotation;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RulesInfoHelper {

    public static boolean isItCollectionRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null;
    }

    public static boolean isItCollectionRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RulesForCollection.class) != null;
    }

    public static boolean isItCustomRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == CustomRule.class;
    }

    public static boolean isItNestedRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == NestedDtoRule.class;
    }

    public static boolean isItRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class) != null;
    }

    public static boolean isItMultipleRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class) != null;
    }

}
