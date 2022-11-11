package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Optional;

import static laoruga.dtogenerator.api.RulesInfoHelper.*;


/**
 * @author Il'dar Valitov
 * Created on 21.07.2022
 */
@Getter
@AllArgsConstructor
@Slf4j
public class RulesInfoExtractor {

    private final FieldGroupFilter fieldsGroupFilter;

    @RequiredArgsConstructor
    static class AnnotationErrorsHandler {

        StringBuilder result = new StringBuilder();
        private final Annotation[] annotations;

        int generalRule = 0;
        int groupOfGeneralRules = 0;
        int collectionRule = 0;
        int groupOfCollectionRules = 0;

        void count() {
            for (Annotation annotation : annotations) {

                if (isItRule(annotation)) {
                    generalRule++;
                }

                if (isItRules(annotation)) {
                    groupOfGeneralRules++;
                }

                if (isItCollectionRule(annotation)) {
                    collectionRule++;
                }

                if (isItCollectionRules(annotation)) {
                    groupOfCollectionRules++;
                }
            }
        }

        void validate() {

            if (generalRule > 1) {
                result.append("Found '" + generalRule + "' @Rule annotations for various types, " +
                        "expected 1 or 0.").append("\n");
            }

            if (groupOfGeneralRules > 1) {
                result.append("Found '" + generalRule + "' @Rules annotations for various types, " +
                        "expected @Rules for single type only.").append("\n");
            }

            if (collectionRule > 1) {
                result.append("Found '" + collectionRule + "' @CollectionRule annotations for various collection types, " +
                        "expected 1 or 0.").append("\n");
            }

            if (groupOfGeneralRules > 1) {
                result.append("Found '" + generalRule + "' @CollectionRules annotations for various collection types, " +
                        "expected @CollectionRules for single collection type only.").append("\n");
            }

            if ((collectionRule + groupOfCollectionRules > 0) && (generalRule + groupOfGeneralRules == 0)) {
                result.append("Missed @Rule annotation for item of collection.").append("\n");
            }

        }

    }

    /**
     * @param field - field whose annotations are to be examined
     * @return - rules for generation value of the field
     */
    Optional<IRuleInfo> checkAndWrapAnnotations(Field field) {

        RuleInfoBuilder ruleInfoBuilder = new RuleInfoBuilder();

        for (Annotation annotation : field.getDeclaredAnnotations()) {

            if (isItRule(annotation)) {

                ruleInfoBuilder
                        .rule(annotation)
                        .ruleType(RuleType.getType(annotation))
                        .rulesGrouped(false)
                        .groupName(getGroupNameFromRuleAnnotation(annotation));

            } else if (isItRules(annotation)) {

                Optional<Pair<String, Annotation>> groupAndRule = selectRuleByGroup(annotation);
                if (groupAndRule.isPresent()) {
                    ruleInfoBuilder
                            .rule(groupAndRule.get().getSecond())
                            .ruleType(RuleType.getType(groupAndRule.get().getSecond()))
                            .groupName(groupAndRule.get().getFirst())
                            .rulesGrouped(true);
                } else {
                    log.debug("None of the rules matched the group for field");
                }

            } else if (isItCollectionRule(annotation)) {

                RuleInfoBuilder collectionRuleInfo = RuleInfo.builder();
                ruleInfoBuilder
                        .collectionRuleInfoBuilder(
                                collectionRuleInfo
                                        .rule(annotation)
                                        .ruleType(RuleType.getType(annotation))
                                        .rulesGrouped(false)
                                        .groupName(getGroupNameFromRuleAnnotation(annotation)))
                        .setAsserter(() -> RulesInfoHelper.checkItemGeneratorCompatibility(
                                ruleInfoBuilder.getRule(),
                                collectionRuleInfo.getRule(),
                                field));

            } else if (isItCollectionRules(annotation)) {

                Optional<Pair<String, Annotation>> groupAndRule = selectRuleByGroup(annotation);
                if (groupAndRule.isPresent()) {
                    RuleInfoBuilder collectionRuleInfo = RuleInfo.builder();
                    ruleInfoBuilder
                            .collectionRuleInfoBuilder(
                                    collectionRuleInfo
                                            .rule(groupAndRule.get().getSecond())
                                            .ruleType(RuleType.getType(groupAndRule.get().getSecond()))
                                            .groupName(groupAndRule.get().getFirst())
                                            .rulesGrouped(true))
                            .setAsserter(() -> RulesInfoHelper.checkItemGeneratorCompatibility(
                                    ruleInfoBuilder.getRule(),
                                    collectionRuleInfo.getRule(),
                                    field));
                } else {
                    log.debug("None of the rules matched the group for field");
                }

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

    private boolean skipByGroup(IRuleInfo ruleInfo) {
        return !getFieldsGroupFilter().isContainsIncludeGroup(ruleInfo.getGroup());
    }

    private Optional<Pair<String, Annotation>> selectRuleByGroup(Annotation rules) {
        try {
            LinkedList<Object> uniqueGroups = new LinkedList<>();
            Object ruleAnnotationsArray = rules.getClass().getMethod("value").invoke(rules);
            int length = Array.getLength(ruleAnnotationsArray);
            Optional<Pair<String, Annotation>> matched = Optional.empty();
            for (int i = 0; i < length; i++) {
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
