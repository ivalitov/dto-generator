package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.meta.Rule;
import laoruga.dtogenerator.api.markup.rules.meta.RuleForCollection;
import laoruga.dtogenerator.api.markup.rules.meta.Rules;
import laoruga.dtogenerator.api.markup.rules.meta.RulesForCollection;
import laoruga.dtogenerator.api.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
public class RulesInfoHelper {

    public static void checkIfItemRuleExist(Collection<RuleInfo> foundRules) {
        if (foundRules.size() != 2) {
            throw new DtoGeneratorException(
                    "Unexpected number of rule Annotations for collection: " + foundRules.size() + ", expected: 2\n." +
                            "Found annotations: " + foundRules.stream().map(RuleInfo::getRule));
        }
        long differentGroupsCount = foundRules.stream().map(RuleInfo::getGroup).distinct().count();
       // TODO проверку групп надо сделать в билдере
          if (differentGroupsCount > 1) {
            throw new DtoGeneratorException(
                    "Collection and item rules groups have matched with different include filters:\n" + foundRules);
        }

    }

    public static void checkItemGeneratorCompatibility(Annotation itemRuleInfo,
                                                       Annotation collectionRuleInfo,
                                                       Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        if (itemRuleInfo != null) {
            Class<?> type = collectionRuleInfo == null ? fieldType : ReflectionUtils.getGenericType(field);
            if (!checkGeneratorCompatibility(type, itemRuleInfo)) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with inappropriate generation " +
                        "rule annotation: '" + itemRuleInfo.annotationType() + "'.");
            }
        }

        // Checking of Collection Generator Compatibility
        if (collectionRuleInfo != null) {
            if (!checkGeneratorCompatibility(fieldType, collectionRuleInfo)) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with inappropriate generation " +
                        "rule annotation: '" + collectionRuleInfo.annotationType() + "'.");
            }
        }
    }

    public static boolean checkGeneratorCompatibility(Class<?> fieldType, Annotation rules) {
        try {
            Class<? extends Annotation> rulesAnnotationClass = rules.annotationType();
            Class<?>[] applicableTypes = (Class<?>[]) rulesAnnotationClass.getField("APPLICABLE_TYPES")
                    .get(rulesAnnotationClass);
            for (Class<?> applicableType : applicableTypes) {
                if (applicableType == fieldType || applicableType.isAssignableFrom(fieldType)) {
                    return true;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get APPLICABLE_TYPES from rules annotation '"
                    + rules + "'", e);
        }
        return false;
    }

    public static boolean isItCollectionRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null;
    }

    public static boolean isItCollectionRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RulesForCollection.class) != null;
    }

    public static boolean isItCustomRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == CustomGenerator.class;
    }

    public static boolean isItNestedRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == NestedDtoRules.class;
    }


    public static boolean isItRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class) != null;
    }

    public static boolean isItRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class) != null;
    }

}
