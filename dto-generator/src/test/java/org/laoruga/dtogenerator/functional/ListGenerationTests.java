package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.BooleanRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.NumberRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientDto;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.StringConfigDto;
import org.laoruga.dtogenerator.constants.RulesInstance;

import java.util.*;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */
@DisplayName("List Type Generators Tests")
@Epic("COLLECTION_RULES")
@ExtendWith(Extensions.RestoreStaticConfig.class)
class ListGenerationTests {

    @Getter
    @NoArgsConstructor
    static class DtoList {

        @CollectionRule(collectionClass = LinkedList.class)
        @StringRule
        private List<String> linkedListOfStrings;

        @CollectionRule(collectionClass = Vector.class)
        @StringRule
        private List<String> vectorOfStrings;

        @CollectionRule
        @StringRule
        private ArrayList<String> arrayListOfStringsImplicit;

        @CollectionRule(collectionClass = LinkedList.class)
        @StringRule
        private LinkedList<String> linkedListOfStringsImplicit;
    }

    @Test
    @DisplayName("List Of Integer Generation (default rules params)")
    void listOfIntegerWithDefaultRulesPrams() {
        ClientDto dto = DtoGenerator.builder(ClientDto.class).build().generateDto();

        assertNotNull(dto);
        List<Integer> numbers = dto.getArrayListIntegerRules();
        assertThat(numbers.size(), both(
                greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize())));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));
        }
    }


    @Test
    @DisplayName("List Of Integer Generation (explicit rules params)")
    void listOfIntegerWithExplicitRulesPrams() {
        ClientDto dto = DtoGenerator.builder(ClientDto.class).build().generateDto();

        assertNotNull(dto);
        List<Integer> numbers = dto.getLinkedListExplicitRules();
        assertNotNull(numbers);
        assertAll(
                () -> assertEquals(LinkedList.class, numbers.getClass()),
                () -> assertEquals(5, numbers.size())
        );
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(2)));
        }
    }

    @Test
    @DisplayName("List Of Strings Generation (explicit rules params)")
    void listOfStingsWithExplicitRulesPrams() {
        DtoList dto = DtoGenerator.builder(DtoList.class).build().generateDto();

        assertNotNull(dto);

        Consumer<List<String>> assertListOfStrings = (list) -> {
            assertThat(list.size(), both(
                    greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize()))
                    .and(lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize())));
            assertThat(list, everyItem(
                    notNullValue()
            ));

            for (String str : list) {
                assertThat(str.length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength())));
            }
        };

        assertAll(
                () -> assertEquals(LinkedList.class, dto.getLinkedListOfStrings().getClass()),
                () -> assertEquals(LinkedList.class, dto.getLinkedListOfStringsImplicit().getClass()),
                () -> assertEquals(ArrayList.class, dto.getArrayListOfStringsImplicit().getClass()),
                () -> assertEquals(Vector.class, dto.getVectorOfStrings().getClass())
        );

        assertAll(
                () -> assertListOfStrings.accept(dto.getLinkedListOfStrings()),
                () -> assertListOfStrings.accept(dto.getLinkedListOfStringsImplicit()),
                () -> assertListOfStrings.accept(dto.getArrayListOfStringsImplicit()),
                () -> assertListOfStrings.accept(dto.getVectorOfStrings())
        );
    }

    @Getter
    static class DtoVariousTypes {

        @CollectionRule(minSize = 10)
        @BooleanRule(trueProbability = 1)
        private List<Boolean> listOfBoolean;

    }

    @Test
    @DisplayName("Various Element Types")
    void variousElementTypes() {

        DtoVariousTypes dto = DtoGenerator.builder(new DtoVariousTypes()).build().generateDto();

        assertAll(
                () -> assertThat(dto.getListOfBoolean().stream().filter(i -> i).count(), equalTo(10L))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldConfig() {

        DtoGeneratorBuilder<DtoList> builder = DtoGenerator.builder(DtoList.class);

        builder.setTypeGeneratorConfig("linkedListOfStrings", CollectionConfigDto.builder().maxSize(2).minSize(2).build());
        builder.setTypeGeneratorConfig("linkedListOfStrings", StringConfigDto.builder().words(new String[]{"PEACE"}).build());
        builder.setTypeGeneratorConfig("vectorOfStrings", StringConfigDto.builder().words(new String[]{"LIFE"}).build());
        builder.setTypeGeneratorConfig("linkedListOfStringsImplicit", CollectionConfigDto.builder().maxSize(3).minSize(3).build());

        builder.getTypeGeneratorConfig().getCollectionConfig(List.class).setMinSize(1);
        DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getCollectionConfig(List.class).setMaxSize(1);

        DtoList dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.linkedListOfStrings, hasSize(2)),
                () -> assertThat(dto.linkedListOfStrings, everyItem(equalTo("PEACE"))),
                () -> assertThat(dto.vectorOfStrings, hasSize(1)),
                () -> assertThat(dto.vectorOfStrings, everyItem(equalTo("LIFE"))),
                () -> assertThat(dto.arrayListOfStringsImplicit, hasSize(1)),
                () -> assertThat(dto.linkedListOfStringsImplicit, hasSize(3))
        );

    }

    @Getter
    static class DtoWithWildcardList {
        @CollectionRule
        @StringRule
        List<?> wildCardList;
    }

    @Getter
    static class DtoWithRawList {
        @CollectionRule
        @NumberRule
        List rawList;
    }

    @Getter
    static class DtoWithListOfCollections {
        @CollectionRule()
        @CollectionRule()
        List<Set<String>> listOfSet;

        @CollectionRule(collectionClass = LinkedList.class)
        List<String> listOfString;
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Wildcard generic type")
    void wildcardGenericType() {
        DtoGenerator<DtoWithWildcardList> generator = DtoGenerator.builder(DtoWithWildcardList.class).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = UtilsRoot.getErrorsMap(generator);

        assertEquals(1, errorsMap.size());
        assertTrue(errorsMap.containsKey("wildCardList"));
        assertThat(errorsMap.get("wildCardList").getCause().getMessage(),
                containsString("Next type must have single generic type: 'java.util.List<?>'"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Raw list")
    void rawList() {
        DtoGenerator<DtoWithRawList> generator = DtoGenerator.builder(DtoWithRawList.class).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = UtilsRoot.getErrorsMap(generator);

        assertEquals(1, errorsMap.size());
        assertTrue(errorsMap.containsKey("rawList"));
        assertThat(errorsMap.get("rawList").getCause().getMessage(),
                containsString("Next type must have single generic type: 'java.util.List'"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("List Of Collection")
    void listOfCollection() {
        DtoGenerator<DtoWithListOfCollections> generator = DtoGenerator.builder(DtoWithListOfCollections.class).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = UtilsRoot.getErrorsMap(generator);

        final String ERROR_MSG_PART = "Missed @Rule annotation for collection element";

        assertEquals(2, errorsMap.size());
        assertTrue(errorsMap.containsKey("listOfSet"));
        assertThat(errorsMap.get("listOfSet").getCause().getMessage(),
                containsString(ERROR_MSG_PART));
        assertTrue(errorsMap.containsKey("listOfString"));
        assertThat(errorsMap.get("listOfString").getCause().getMessage(),
                containsString(ERROR_MSG_PART));

    }

    static class Dto3 {

        @CollectionRule
        @NumberRule
        List<String> some;

    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Wrong Element Rule Annotation")
    void wrongElementRuleAnnotation() {

        DtoGenerator<Dto3> generator = DtoGenerator.builder(Dto3.class).build();

        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = UtilsRoot.getErrorsMap(generator);

        assertThat(errorsMap.size(), equalTo(1));
        assertThat(errorsMap.get("some").getCause().getMessage(),
                stringContainsInOrder("Wrong collection element type"));
    }

    @Getter
    @NoArgsConstructor
    static class DtoSet {
        @CollectionRule
        @NumberRule
        Set<Integer> numbers;

        @CollectionRule(collectionClass = LinkedHashSet.class)
        @NumberRule
        Set<Integer> linkedHashSet;

        @CollectionRule(collectionClass = TreeSet.class)
        @NumberRule
        TreeSet<Integer> treeSet;

        @CollectionRule
        @NumberRule
        HashSet<Integer> hashSet;
    }

    @Test
    @DisplayName("Set Of Integers Generation (default rules params)")
    void setOfIntegerWithDefaultRulesPrams() {
        DtoSet dto = DtoGenerator.builder(DtoSet.class).build().generateDto();

        assertNotNull(dto);
        checkNumbers(dto.getNumbers());
        assertEquals(HashSet.class, dto.getNumbers().getClass());

        checkNumbers(dto.getLinkedHashSet());
        assertEquals(LinkedHashSet.class, dto.getLinkedHashSet().getClass());

        checkNumbers(dto.getTreeSet());
        checkNumbers(dto.getHashSet());

    }

    private static void checkNumbers(Set<Integer> numbers) {
        assertThat(numbers.size(), both(
                greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize())));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                    .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));
        }
    }

}
