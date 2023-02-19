package org.laoruga.dtogenerator.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.laoruga.dtogenerator.FieldFilter;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


/**
 *
 * @author Il'dar Valitov
 * Created on 21.07.2022
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class RulesInfoExtractor {

    private final FieldFilter fieldsGroupFilter;
    private RuleInfoBuilder ruleInfoBuilder;

    /**
     * Extracts rules information from {@link Rule} and {@link Rules} annotations of the field
     * considering the group {@link FieldFilter}.
     * <p>
     * This method validates if {@link Rule} annotations represented in the correct quantity.
     * But doesn't validate whether {@link Rule} annotations matched to the type of the field or not.
     *
     * @param field - field containing {@link Rule} and/or {@link Rules} annotations
     * @return - an empty Optional if Rules excluded by group filter {@link FieldFilter},
     * otherwise {@link IRuleInfo} object containing rules information.
     * @throws DtoGeneratorValidationException - if rules annotations quantity check failed.
     */
    synchronized public Optional<IRuleInfo> extractRulesInfo(Field field) throws DtoGeneratorValidationException {

        RuleAnnotationsValidationHelper.validate(field.getAnnotations());

        ruleInfoBuilder = new RuleInfoBuilder();

        for (Annotation annotation : field.getDeclaredAnnotations()) {

            switch (RulesInfoHelper.getHelperType(annotation)) {

                case RULE:
                    extractRuleInfo(annotation);
                    break;

                case RULE_FOR_COLLECTION:
                    extractCollectionRuleInfo(annotation);
                    break;

                case RULES:
                    extractRuleInfoFromMultipleRules(annotation);
                    break;

                case RULES_FOR_COLLECTION:
                    extractCollectionRuleInfoFromMultipleRules(annotation);
                    break;

                case UNKNOWN:
                    log.debug("Unknown annotation: '" + annotation.annotationType().getName()
                            + "' of field: '" + field.getType() + " " + field.getName() + "'");
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected helper rule type: "
                            + RulesInfoHelper.getHelperType(annotation));
            }

        }

        if (ruleInfoBuilder.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ruleInfoBuilder.build());
    }

    private void extractRuleInfo(Annotation rule) {
        String groupName = getGroupNameFromRuleAnnotation(rule);
        if (!skipByGroup(groupName)) {
            ruleInfoBuilder
                    .rule(rule)
                    .ruleType(RuleType.getType(rule))
                    .multipleRules(false)
                    .groupName(groupName);
        }
    }

    private void extractCollectionRuleInfo(Annotation rule) {
        String groupName = getGroupNameFromRuleAnnotation(rule);
        if (!skipByGroup(groupName)) {
            RuleInfoBuilder collectionRuleInfo = RuleInfo.builder();
            ruleInfoBuilder
                    .collectionRuleInfoBuilder(
                            collectionRuleInfo
                                    .rule(rule)
                                    .ruleType(RuleType.getType(rule))
                                    .multipleRules(false)
                                    .groupName(groupName));
        }
    }

    private void extractRuleInfoFromMultipleRules(Annotation multipleRules) {
        selectRuleByGroup(multipleRules).ifPresent(groupAndRule ->
                ruleInfoBuilder
                        .rule(groupAndRule.getSecond())
                        .ruleType(RuleType.getType(groupAndRule.getSecond()))
                        .groupName(groupAndRule.getFirst())
                        .multipleRules(true)
        );
    }

    private void extractCollectionRuleInfoFromMultipleRules(Annotation multipleRules) {
        selectRuleByGroup(multipleRules).ifPresent(groupAndRule ->
                ruleInfoBuilder.collectionRuleInfoBuilder(
                        RuleInfo.builder()
                                .rule(groupAndRule.getSecond())
                                .ruleType(RuleType.getType(groupAndRule.getSecond()))
                                .groupName(groupAndRule.getFirst())
                                .multipleRules(true))
        );
    }

    private boolean skipByGroup(String groupName) {
        return !getFieldsGroupFilter().isContainsIncludeGroup(groupName);
    }

    private Optional<Pair<String, Annotation>> selectRuleByGroup(Annotation rules) {
        try {
            Set<Object> uniqueGroups = new HashSet<>();
            Object ruleAnnotationsArray = rules.getClass().getMethod("value").invoke(rules);

            Optional<Pair<String, Annotation>> matched = Optional.empty();
            for (int i = 0; i < Array.getLength(ruleAnnotationsArray); i++) {
                Annotation rule = (Annotation) Array.get(ruleAnnotationsArray, i);
                String checkedGroup = getGroupNameFromRuleAnnotation(rule);
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

    private static String getGroupNameFromRuleAnnotation(Annotation rule) {
        try {
            return (String) rule.annotationType().getMethod("group").invoke(rule);
        } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
        }
    }


}
