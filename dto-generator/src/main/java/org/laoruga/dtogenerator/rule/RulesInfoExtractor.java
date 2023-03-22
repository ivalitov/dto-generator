package org.laoruga.dtogenerator.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.laoruga.dtogenerator.FieldFilter;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorValidationException;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.laoruga.dtogenerator.rule.RulesInfoHelper.validateType;

/**
 * Extracts information of generation rules and validates, for further processing.
 *
 * @author Il'dar Valitov
 * Created on 21.07.2022
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class RulesInfoExtractor {

    private final FieldFilter fieldsGroupFilter;

    private Field field;

    /**
     * Extracts information from {@link Rule} and {@link Rules} annotations of the field
     * considering the filtering group {@link FieldFilter}.
     * <p>
     * Method validates:
     * - if {@link Rule} annotations are represented in the correct quantity;
     * - if {@link Rule} annotations matched to the type of the field.
     * <p>
     * Method is synchronized, it works with {@link RulesInfoExtractor#field}.
     *
     * @param fieldToInspect - field containing {@link Rule} and/or {@link Rules} annotations
     * @return - an empty Optional if Rules excluded by group filter {@link FieldFilter},
     * otherwise {@link IRuleInfo} object containing rules information.
     * @throws DtoGeneratorValidationException - if rules annotations quantity check failed.
     */
    synchronized public Optional<IRuleInfo> extractRulesInfo(Field fieldToInspect) throws DtoGeneratorValidationException {

        this.field = fieldToInspect;

        Annotation annotation = RulesInfoHelper.getSingleRulesOrNull(field.getAnnotations());

        if (annotation == null) {
            log.debug("No rule annotations found on the field: '" + field);
            return Optional.empty();
        }

        IRuleInfo ruleInfo = null;
        
        switch (RulesInfoHelper.getHelperType(annotation)) {

            case RULE:
            case NESTED_DTO_RULE:
            case CUSTOM_RULE:
                String groupName = RulesInfoHelper.getGroupNameFromRuleAnnotation(annotation);
                if (!skipByGroup(groupName)) {
                    ruleInfo = buildRuleInfo(annotation, field.getType(), groupName, false);
                }
                break;

            case RULE_FOR_COLLECTION:
                groupName = RulesInfoHelper.getGroupNameFromRuleAnnotation(annotation);
                if (!skipByGroup(groupName)) {
                    ruleInfo = buildCollectionRuleInfo(
                            (CollectionRule) annotation, groupName, false);
                }
                break;

            case RULE_FOR_MAP:
                groupName = RulesInfoHelper.getGroupNameFromRuleAnnotation(annotation);
                ruleInfo = buildMapRuleInfo(
                        (MapRule) annotation, groupName, false);
                break;

            case RULES:
                Optional<Pair<String, Annotation>> maybeGroupAndRules = selectRuleByGroup(annotation);
                if (maybeGroupAndRules.isPresent()) {
                    Pair<String, Annotation> groupAndRule = maybeGroupAndRules.get();
                    ruleInfo = buildRuleInfo(
                            groupAndRule.getSecond(), field.getType(), groupAndRule.getFirst(), true);
                }
                break;

            case RULES_FOR_COLLECTION:
                maybeGroupAndRules = selectRuleByGroup(annotation);
                if (maybeGroupAndRules.isPresent()) {
                    Pair<String, Annotation> groupAndRule = maybeGroupAndRules.get();
                    ruleInfo = buildCollectionRuleInfo(
                            (CollectionRule) groupAndRule.getSecond(), groupAndRule.getFirst(), true);
                }
                break;

            case RULES_FOR_MAP:
                maybeGroupAndRules = selectRuleByGroup(annotation);
                if (maybeGroupAndRules.isPresent()) {
                    Pair<String, Annotation> groupAndRule = maybeGroupAndRules.get();
                    ruleInfo = buildMapRuleInfo(
                            (MapRule) groupAndRule.getSecond(), groupAndRule.getFirst(), true);
                }
                break;

            case UNKNOWN:
                log.debug("Unknown annotation: '" + annotation.annotationType().getName()
                        + "' of field: '" + field.getType() + " " + field.getName() + "'");
                break;

            default:
                throw new IllegalArgumentException("Unexpected helper rule type: "
                        + RulesInfoHelper.getHelperType(annotation));
        }

        return Optional.ofNullable(ruleInfo);
    }

    private RuleInfo buildRuleInfo(Annotation rule, Class<?> requiredType, String groupName, boolean isMultipleRules) {

        validateType(requiredType, rule);

        return RuleInfo.builder()
                .rule(rule)
                .ruleType(RuleType.getType(rule))
                .multipleRules(isMultipleRules)
                .group(groupName)
                .build();
    }

    private RuleInfoCollection buildCollectionRuleInfo(CollectionRule collectionRule,
                                                       String groupName,
                                                       boolean isMultipleRules) {

        Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
        Annotation elementRule = ReflectionUtils.getSingleRuleFromEntry(collectionRule.element());

        validateType(elementType, elementRule);
        validateType(field.getType(), collectionRule);

        RuleInfo collectionElementInfo = buildRuleInfo(elementRule, elementType, groupName, false);

        RuleInfo collectionInfo = RuleInfo.builder()
                .rule(collectionRule)
                .ruleType(RuleType.getType(collectionRule))
                .multipleRules(isMultipleRules)
                .group(groupName)
                .build();

        return RuleInfoCollection.builder()
                .collectionRuleInfo(collectionInfo)
                .elementRuleInfo(collectionElementInfo)
                .group(groupName)
                .build();

    }

    private RuleInfoMap buildMapRuleInfo(MapRule mapRule, String groupName, boolean isMultipleRules) {

        Class<?>[] keyValueTypes = ReflectionUtils.getPairedGenericType(field);

        Annotation keyRule = ReflectionUtils.getSingleRuleFromEntry(mapRule.key());
        Annotation valueRule = ReflectionUtils.getSingleRuleFromEntry(mapRule.value());

        validateType(field.getType(), mapRule);
        validateType(keyValueTypes[0], keyRule);
        validateType(keyValueTypes[1], valueRule);

        RuleInfo mapKeyRuleInfo =
                buildRuleInfo(keyRule, keyValueTypes[0], groupName, false);

        RuleInfo mapValueRuleInfo =
                buildRuleInfo(valueRule, keyValueTypes[1], groupName, false);

        RuleInfo mapRuleInfo = RuleInfo.builder()
                .rule(mapRule)
                .ruleType(RuleType.getType(mapRule))
                .multipleRules(isMultipleRules)
                .group(groupName)
                .build();

        return RuleInfoMap.builder()
                .mapRule(mapRuleInfo)
                .keyRule(mapKeyRuleInfo)
                .valueRule(mapValueRuleInfo)
                .group(groupName)
                .build();
    }

    private boolean skipByGroup(String groupName) {
        return !fieldsGroupFilter.isContainsIncludeGroup(groupName);
    }

    private Optional<Pair<String, Annotation>> selectRuleByGroup(Annotation rules) {
        try {
            Set<Object> uniqueGroups = new HashSet<>();
            Object ruleAnnotationsArray = rules.getClass().getMethod("value").invoke(rules);

            Optional<Pair<String, Annotation>> matched = Optional.empty();
            for (int i = 0; i < Array.getLength(ruleAnnotationsArray); i++) {
                Annotation rule = (Annotation) Array.get(ruleAnnotationsArray, i);
                String checkedGroup = RulesInfoHelper.getGroupNameFromRuleAnnotation(rule);
                if (uniqueGroups.contains(rule)) {
                    throw new DtoGeneratorException("Rule group '" + checkedGroup + "' is repeating for field.");
                } else {
                    uniqueGroups.add(rule);
                }
                if (getFieldsGroupFilter().isContainsIncludeGroup(checkedGroup)) {
                    if (!matched.isPresent()) {
                        matched = Optional.of(Pair.create(checkedGroup, rule));
                    } else {
                        throw new DtoGeneratorException("Ambiguous grouping of the field." +
                                " Check groups of generators and include filters.");
                    }
                }
            }
            return matched;
        } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
        }
    }

}
