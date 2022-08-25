package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.meta.Rule;
import laoruga.dtogenerator.api.markup.rules.meta.RuleForCollection;
import laoruga.dtogenerator.api.markup.rules.meta.Rules;
import laoruga.dtogenerator.api.markup.rules.meta.RulesForCollection;
import laoruga.dtogenerator.api.util.ReflectionUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static laoruga.dtogenerator.api.Action.GENERATE;
import static laoruga.dtogenerator.api.RuleType.*;

/**
 * @author Il'dar Valitov
 * Created on 21.07.2022
 */
@Getter
@AllArgsConstructor
@Slf4j
public class RulesInfoExtractor {

    private final FieldGroupFilter fieldsGroupFilter;

    @Builder
    @Data
    @ToString
    private static class ParamDto {
        String groupName;
        Annotation rule;
        boolean areRulesGrouped;
        String ruleGroup;
        RuleType ruleType;
    }

    /**
     * Correctness checks and evaluation of field's annotations type.
     * Method extracts generation rules from annotations of passed field.
     * Also, method checks errors of annotation.
     *
     * @param field field to check
     * @return type field's annotations
     */// TODO не возвращать ошибку при отсутсвии одной из двух аннотаций для коллекций
    RulesInfo checkAndWrapRulesInfo(Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        Map<RuleType, ParamDto> foundRules = new HashMap<>();

        RulesInfo.RulesInfoBuilder rulesInfoBuilder = RulesInfo.builder();

        for (Annotation annotation : field.getDeclaredAnnotations()) {
            if (isItRule(annotation)) {
                if (foundRules.containsKey(NOT_COLLECTION)) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated with different type rules annotations");
                }
                foundRules.putIfAbsent(NOT_COLLECTION, ParamDto.builder().rule(annotation).build());
            } else if (isItRules(annotation)) {
                if (foundRules.containsKey(NOT_COLLECTION)) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated more then one rules annotation");
                }
                foundRules.put(NOT_COLLECTION, ParamDto.builder().rule(annotation).areRulesGrouped(true).build());
            } else if (isItCollectionRule(annotation)) {
                if (foundRules.containsKey(COLLECTION)) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated with more then one collection rules annotations");
                }
                foundRules.put(COLLECTION, ParamDto.builder().rule(annotation).build());
            } else if (isItCollectionRules(annotation)) {
                if (foundRules.containsKey(COLLECTION)) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated more then one collection rules annotation");
                }
                foundRules.put(COLLECTION, ParamDto.builder().rule(annotation).areRulesGrouped(true).build());
            }
        }
        if (foundRules.isEmpty()) {
            return rulesInfoBuilder.action(Action.CHECK_EXPLICIT_GENERATOR).build();
        }

        // select rule if exists
        if (foundRules.containsKey(NOT_COLLECTION)) {
            ParamDto paramDto = foundRules.get(NOT_COLLECTION);
            if (paramDto.isAreRulesGrouped()) {
                Pair<String, Annotation> groupAndRule = getRuleByGroupOrNull(paramDto.getRule(), fieldName);
                if (groupAndRule != null) {
                    paramDto.setGroupName(groupAndRule.getFirst());
                    paramDto.setRule(groupAndRule.getSecond());
                } else {
                    log.debug("Not collection rules excluded by group for field: '{}'", fieldName);
                    foundRules.remove(NOT_COLLECTION);
                }
            } else {
                paramDto.setGroupName(getRuleGroup(paramDto.getRule()));
            }
            if (foundRules.containsKey(NOT_COLLECTION)) {
                Annotation rule = paramDto.getRule();
                if (isItCustomRule(rule)) {
                    paramDto.setRuleType(CUSTOM);
                } else if (isItNestedRule(rule)) {
                    paramDto.setRuleType(NESTED);
                } else {
                    paramDto.setRuleType(BASIC);
                }
            }
        }

        // select collection rule
        if (foundRules.containsKey(COLLECTION)) {
            ParamDto paramDto = foundRules.get(COLLECTION);
            if (paramDto.isAreRulesGrouped()) {
                Pair<String, Annotation> groupAndRule = getRuleByGroupOrNull(paramDto.getRule(), fieldName);
                if (groupAndRule != null) {
                    paramDto.setGroupName(groupAndRule.getFirst());
                    paramDto.setRule(groupAndRule.getSecond());
                } else {
                    log.debug("Collection rules excluded by group for field: '{}'", fieldName);
                    foundRules.remove(COLLECTION);
                }
            } else {
                paramDto.setGroupName(getRuleGroup(paramDto.getRule()));
            }
            paramDto.setRuleType(COLLECTION);
        }

        // may be excluded by group filter
        if (foundRules.isEmpty()) {
            return rulesInfoBuilder.action(Action.SKIP).build();
        }

        // check groups equality
        if (foundRules.size() > 1) {
            long differentGroupsCount = foundRules.values().stream().map(ParamDto::getRuleGroup).distinct().count();
            if (differentGroupsCount > 1) {
                throw new DtoGeneratorException(
                        "Collection and item rules groups have matched with different include filters:\n" + foundRules);
            }
        }

        String ruleGroup = foundRules.values().stream().findAny().get().getGroupName();

        // skip by group
        if (!getFieldsGroupFilter().isContainsIncludeGroup(ruleGroup)) {
            return rulesInfoBuilder.action(Action.SKIP).build();
        }

        List<RuleType> ruleTypes = new LinkedList<>();
        rulesInfoBuilder.action(GENERATE);
        rulesInfoBuilder.ruleType(ruleTypes);
        foundRules.forEach((ruleType, paramDto) -> {
                    ruleTypes.add(paramDto.getRuleType());
                    if (ruleType == NOT_COLLECTION) {
                        rulesInfoBuilder.itemGenerationRules(paramDto.getRule());
                    } else if (ruleType == COLLECTION) {
                        rulesInfoBuilder.collectionGenerationRules(paramDto.getRule());
                    } else {
                        throw new RuntimeException("Error. Unexpected rules type: " + ruleType);
                    }
                }
        );

        RulesInfo resultRulesInfo = rulesInfoBuilder.build();


        // Checking Item Generator Compatibility
        if (resultRulesInfo.getItemGenerationRules() != null) {
            Class<?> type = fieldType;
            if (resultRulesInfo.getCollectionGenerationRules() != null) {
                type = ReflectionUtils.getGenericType(field);
            }
            if (!checkGeneratorCompatibility(type, resultRulesInfo.getItemGenerationRules())) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with inappropriate generation " +
                        "rule annotation: '" + resultRulesInfo.getItemGenerationRules().annotationType() + "'.");
            }
        }

        // Checking of Collection Generator Compatibility
        if (resultRulesInfo.getCollectionGenerationRules() != null) {
            if (!checkGeneratorCompatibility(fieldType, resultRulesInfo.getCollectionGenerationRules())) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with inappropriate generation " +
                        "rule annotation: '" + resultRulesInfo.getCollectionGenerationRules().annotationType() + "'.");
            }
        }

        return resultRulesInfo;
    }

    private Pair<String, Annotation> getRuleByGroupOrNull(Annotation rules, String fieldName) {
        try {
            LinkedList<Object> uniqueGroups = new LinkedList<>();
            Object ruleAnnotationsArray = rules.getClass().getMethod("value").invoke(rules);
            int length = Array.getLength(ruleAnnotationsArray);
            Pair<String, Annotation> matched = null;
            for (int i = 0; i < length; i++) {
                Annotation rule = (Annotation) Array.get(ruleAnnotationsArray, i);
                String checkedGroup = getRuleGroup(rule);
                if (uniqueGroups.contains(rule)) {
                    throw new DtoGeneratorException("Rule group '" + checkedGroup + "' is repeating for field '" + fieldName + "'");
                } else {
                    uniqueGroups.add(rule);
                }
                if (getFieldsGroupFilter().isContainsIncludeGroup(checkedGroup)) {
                    if (matched == null) {
                        matched = Pair.create(checkedGroup, rule);
                    } else {
                        throw new DtoGeneratorException("Ambiguous grouping of the field: '" + fieldName + "'." +
                                " Check groups of generators and include filters.");
                    }
                }
            }
            return matched;
        } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
        }
    }

    private static boolean checkGeneratorCompatibility(Class<?> fieldType, Annotation rules) {
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

    private static boolean isItCollectionRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null;
    }

    private static boolean isItCollectionRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RulesForCollection.class) != null;
    }

    private static boolean isItCustomRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == CustomGenerator.class;
    }

    private static boolean isItNestedRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == NestedDtoRules.class;
    }


    private static boolean isItRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class) != null;
    }

    private static boolean isItRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class) != null;
    }

    private static String getRuleGroup(Annotation rule) {
        try {
            return (String) rule.annotationType().getMethod("group").invoke(rule);
        } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
        }
    }

}
