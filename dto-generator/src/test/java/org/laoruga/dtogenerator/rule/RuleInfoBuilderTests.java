package org.laoruga.dtogenerator.rule;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Il'dar Valitov
 * Created on 22.11.2022
 */

@DisplayName("RuleInfoBuilder tests")
@Epic("UNIT_TESTS")
@Feature("RULES_INFO_BUILDER")
class RuleInfoBuilderTests {

    private static final Annotation annotationMock = () -> null;

    @Test
    @DisplayName("Setter validation")
    void setterValidation() {
        assertAll(
                () -> executeSetterTestCase(() ->
                        RuleInfo.builder().rule(annotationMock).rule(annotationMock)),
                () -> executeSetterTestCase(() ->
                        RuleInfo.builder()
                                .collectionRuleInfoBuilder(RuleInfo.builder())
                                .collectionRuleInfoBuilder(RuleInfo.builder())),
                () -> executeSetterTestCase(() ->
                        RuleInfo.builder().ruleType(RuleType.BASIC).ruleType(RuleType.CUSTOM)),
                () -> executeSetterTestCase(() ->
                        RuleInfo.builder().multipleRules(true).multipleRules(false)),
                () -> executeSetterTestCase(() ->
                        RuleInfo.builder().groupName("name").groupName("name"))
        );
    }

    void executeSetterTestCase(Runnable testCase) {
        DtoGeneratorException e = assertThrows(DtoGeneratorException.class, testCase::run);
        assertThat(e.getMessage(), containsString("Field annotated more then one"));
    }


    @TestFactory
    @DisplayName("Build validation")
    Stream<DynamicTest> buildValidation() {
        final String BASE_ERROR = "Failed to construct unit or collection rules info";
        final String COLLECTION_ERROR = "Failed to construct collection rules info";
        final String COLLECTION_ITEM_ERROR = "Failed to construct collection item rules info";
        final String GROUPS_NOT_MATCH = "Unexpected error, collection generator's group not equals to unit";

        RuleInfoBuilder collectionRuleInfoBuilder = RuleInfo.builder()
                .rule(annotationMock)
                .ruleType(RuleType.COLLECTION)
                .groupName("group")
                .multipleRules(true);

        return Stream.of(
                DynamicTest.dynamicTest(BASE_ERROR,
                        () -> executeBuilderTestCase(() -> RuleInfo.builder().build(), BASE_ERROR)),

                DynamicTest.dynamicTest(COLLECTION_ITEM_ERROR,
                        () -> executeBuilderTestCase(() -> RuleInfo.builder()
                                .collectionRuleInfoBuilder(RuleInfo.builder()).build(), COLLECTION_ITEM_ERROR)),

                DynamicTest.dynamicTest(COLLECTION_ERROR,
                        () -> executeBuilderTestCase(() -> RuleInfo.builder()
                                .collectionRuleInfoBuilder(collectionRuleInfoBuilder).build(), COLLECTION_ERROR)),

                DynamicTest.dynamicTest(GROUPS_NOT_MATCH,
                        () -> executeBuilderTestCase(() -> RuleInfo.builder()
                                        .rule(annotationMock)
                                        .ruleType(RuleType.BASIC)
                                        .groupName("wrong_group_name")
                                        .multipleRules(true)
                                        .collectionRuleInfoBuilder(collectionRuleInfoBuilder)
                                        .build(),
                                GROUPS_NOT_MATCH))
        );

    }

    void executeBuilderTestCase(Runnable testCase, String errorMsgPart) {
        DtoGeneratorException e = assertThrows(DtoGeneratorException.class, testCase::run);
        assertThat(e.getMessage(), containsString(errorMsgPart));
    }

}
