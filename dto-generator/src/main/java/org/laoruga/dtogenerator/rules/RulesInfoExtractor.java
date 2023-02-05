package org.laoruga.dtogenerator.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.laoruga.dtogenerator.FieldGroupFilter;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.laoruga.dtogenerator.rules.RulesInfoHelper.*;


/**
 * @author Il'dar Valitov
 * Created on 21.07.2022
 */
@Getter
@AllArgsConstructor
@Slf4j
public class RulesInfoExtractor {

    private final FieldGroupFilter fieldsGroupFilter;

    /**
     * @param field            - field whose annotations are to be examined
     * @return - rules for generation value of the field
     */
    public Optional<IRuleInfo> checkAndWrapAnnotations(Field field) {

        RuleInfoBuilder ruleInfoBuilder = new RuleInfoBuilder();

        for (Annotation annotation : field.getDeclaredAnnotations()) {

            if (isItRule(annotation)) {

                extractRuleInfo(ruleInfoBuilder, annotation);

            } else if (isItMultipleRules(annotation)) {

                extractRuleInfoFromMultipleRules(ruleInfoBuilder, annotation);

            } else if (isItCollectionRule(annotation)) {

                extractCollectionRuleInfo(ruleInfoBuilder, annotation);

            } else if (isItCollectionRules(annotation)) {

                extractCollectionRuleInfoFromMultipleRules(ruleInfoBuilder, annotation);

            }
        }

        if (ruleInfoBuilder.isEmpty()) {
            return Optional.empty();
        }

        IRuleInfo ruleInfo = ruleInfoBuilder.build();

        if (skipByGroup(ruleInfo)) {
            return Optional.empty();
        }

        return Optional.of(ruleInfo);
    }

    private static final String ERROR_MSG_PATTERN = "Inappropriate generation rule annotation: '%s' for field: '%s'";

    private void extractRuleInfo(RuleInfoBuilder ruleInfoBuilder,
                                 Annotation rule) {
        ruleInfoBuilder
                .rule(rule)
                .ruleType(RuleType.getType(rule))
                .multipleRules(false)
                .groupName(getGroupNameFromRuleAnnotation(rule));
    }

    private void extractRuleInfoFromMultipleRules(RuleInfoBuilder ruleInfoBuilder,
                                                  Annotation rule) {
        Optional<Pair<String, Annotation>> groupAndRule = selectRuleByGroup(rule);
        if (groupAndRule.isPresent()) {
            Annotation ruleSelectedByGroup = groupAndRule.get().getSecond();
            ruleInfoBuilder
                    .rule(ruleSelectedByGroup)
                    .ruleType(RuleType.getType(ruleSelectedByGroup))
                    .groupName(groupAndRule.get().getFirst())
                    .multipleRules(true);
        }
    }

    private void extractCollectionRuleInfo(RuleInfoBuilder ruleInfoBuilder,
                                           Annotation rule) {
        RuleInfoBuilder collectionRuleInfo = RuleInfo.builder();
        ruleInfoBuilder
                .collectionRuleInfoBuilder(
                        collectionRuleInfo
                                .rule(rule)
                                .ruleType(RuleType.getType(rule))
                                .multipleRules(false)
                                .groupName(getGroupNameFromRuleAnnotation(rule)));
    }

    private void extractCollectionRuleInfoFromMultipleRules(RuleInfoBuilder ruleInfoBuilder,
                                                            Annotation rule) {
        Optional<Pair<String, Annotation>> groupAndRule = selectRuleByGroup(rule);
        if (groupAndRule.isPresent()) {
            RuleInfoBuilder collectionRuleInfo = RuleInfo.builder();
            ruleInfoBuilder
                    .collectionRuleInfoBuilder(
                            collectionRuleInfo
                                    .rule(groupAndRule.get().getSecond())
                                    .ruleType(RuleType.getType(groupAndRule.get().getSecond()))
                                    .groupName(groupAndRule.get().getFirst())
                                    .multipleRules(true));
        }
    }

    private boolean skipByGroup(IRuleInfo ruleInfo) {
        return !getFieldsGroupFilter().isContainsIncludeGroup(ruleInfo.getGroup());
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
