package org.laoruga.dtogenerator.rule;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

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

    public static void validateType(Class<?> generatedType, Annotation rulesAnnotation) {
        generatedType = Primitives.wrap(generatedType);

        Class<?>[] possibleGeneratedTypes = GeneratedTypes.get(rulesAnnotation.annotationType());

        boolean match = false;
        for (Class<?> knownElementType : possibleGeneratedTypes) {
            if (knownElementType.isAssignableFrom(generatedType)) {
                match = true;
                break;
            }
        }

        if (!match) {
            throw new DtoGeneratorException("Field type or generic type: '" + generatedType + "'"
                    + " does not match to rules annotation: '@" + rulesAnnotation.annotationType().getSimpleName() + "'"
                    + " Expected types of the field:\n" + Arrays.asList(possibleGeneratedTypes) + "\n");
        }
    }

    public static Annotation getSingleRulesOrNull(Annotation[] annotations) {

        Annotation selected = null;

        for (Annotation annotation : annotations) {

            RuleTypeHelper helperType = getHelperType(annotation);
            if (helperType != UNKNOWN) {
                if (selected != null) {
                    throw new DtoGeneratorValidationException(
                            "Found @Rule annotations at least for 2 different types: " +
                                    "'" + selected.annotationType().getSimpleName() + "'" +
                                    "'" + annotation.annotationType().getSimpleName() + "'");
                }
                selected = annotation;
            }

        }

        return selected;
    }

    static String getGroupNameFromRuleAnnotation(Annotation rule) {
        try {
            return (String) rule.annotationType().getMethod("group").invoke(rule);
        } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
        }
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
