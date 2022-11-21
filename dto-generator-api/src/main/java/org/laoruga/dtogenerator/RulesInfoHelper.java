package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRules;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.RuleForCollection;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.api.rules.meta.RulesForCollection;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RulesInfoHelper {

    public static void checkItemGeneratorCompatibility(Annotation itemRuleInfo,
                                                       Annotation collectionRuleInfo,
                                                       Field field) {
        Class<?> fieldType = field.getType();

        if (itemRuleInfo != null) {
            Class<?> type = collectionRuleInfo == null ? fieldType : ReflectionUtils.getSingleGenericType(field);
            checkGeneratorCompatibility(type, itemRuleInfo);
        }

        // Checking of Collection Generator Compatibility
        if (collectionRuleInfo != null) {
            checkGeneratorCompatibility(fieldType, collectionRuleInfo);
        }
    }

    public static void checkGeneratorCompatibility(Class<?> fieldType, Annotation rule) {
        try {
            Class<? extends Annotation> rulesAnnotationClass = rule.annotationType();
            Class<?>[] applicableTypes = (Class<?>[]) rulesAnnotationClass.getField("APPLICABLE_TYPES")
                    .get(rulesAnnotationClass);
            for (Class<?> applicableType : applicableTypes) {
                if (applicableType == fieldType || applicableType.isAssignableFrom(fieldType)) {
                    return;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get APPLICABLE_TYPES from rules annotation '"
                    + rule + "'", e);
        }
        throw new DtoGeneratorException("Inappropriate generation rule annotation: '" + rule + "'");
    }

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
        return ruleAnnotation.annotationType() == NestedDtoRules.class;
    }


    public static boolean isItRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class) != null;
    }

    public static boolean isItMultipleRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class) != null;
    }

}
