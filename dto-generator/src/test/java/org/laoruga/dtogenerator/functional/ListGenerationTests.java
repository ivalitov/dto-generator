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
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.CollectionConfig;
import org.laoruga.dtogenerator.generator.config.dto.StringConfig;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

        @CollectionRule(
                collectionClass = LinkedList.class)
        private List<String> linkedListOfStrings;

        @CollectionRule(
                collectionClass = Vector.class)
        private List<String> vectorOfStrings;

        @CollectionRule
        private ArrayList<String> arrayListOfStringsImplicit;

        @CollectionRule(
                collectionClass = LinkedList.class)
        private LinkedList<String> linkedListOfStringsImplicit;

        @CollectionRule
        private List<AtomicInteger> listOfAtomicInteger;
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

        @CollectionRule(minSize = 10, element = @Entry(booleanRule =
        @BooleanRule(trueProbability = 1)))
        private List<Boolean> listOfBoolean;

        @CollectionRule(minSize = 2, maxSize = 2, element = @Entry(numberRule =
        @NumberRule(minInt = 777, maxInt = 777)))
        private List<AtomicInteger> listOfAtomicInteger;

        @CollectionRule(collectionClass = LinkedList.class, minSize = 10, element = @Entry(numberRule =
        @NumberRule(minInt = 1, maxInt = 1)))
        private List<Integer> listOfInteger;

    }

    @Test
    @DisplayName("Various Element Types")
    void variousElementTypes() {

        DtoVariousTypes dto = DtoGenerator.builder(new DtoVariousTypes()).build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfBoolean.stream().filter(i -> i).count(), equalTo(10L)),
                () -> assertThat(
                        dto.listOfAtomicInteger.stream().map(AtomicInteger::get).collect(Collectors.toList()),
                        everyItem(equalTo(777))),
                () -> assertThat(dto.listOfInteger, isA(LinkedList.class)),
                () -> assertThat(dto.listOfInteger, everyItem(equalTo(1)))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldConfig() {

        DtoGeneratorBuilder<DtoList> builder = DtoGenerator.builder(DtoList.class);

        // static
        builder.getStaticConfig().getTypeGeneratorsConfig().getCollectionConfig(List.class).setMaxSize(1);
        builder.getStaticConfig().getTypeGeneratorsConfig().getNumberConfig().setMaxIntValue(1).setMinIntValue(1);

        // instance
        builder.setGeneratorConfig("linkedListOfStrings", CollectionConfig.builder().maxSize(2).minSize(2).build())
                .setGeneratorConfig("linkedListOfStrings", StringConfig.builder().words(new String[]{"PEACE"}).build())
                .setGeneratorConfig("vectorOfStrings", StringConfig.builder().words(new String[]{"LIFE"}).build())
                .setGeneratorConfig("linkedListOfStringsImplicit", CollectionConfig.builder().maxSize(3).minSize(3).build());

        builder.getConfig().getTypeGeneratorsConfig().getCollectionConfig(List.class).setMinSize(1);

        DtoList dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.linkedListOfStrings, hasSize(2)),
                () -> assertThat(dto.linkedListOfStrings, everyItem(equalTo("PEACE"))),
                () -> assertThat(dto.vectorOfStrings, hasSize(1)),
                () -> assertThat(dto.vectorOfStrings, everyItem(equalTo("LIFE"))),
                () -> assertThat(dto.arrayListOfStringsImplicit, hasSize(1)),
                () -> assertThat(dto.linkedListOfStringsImplicit, hasSize(3)),
                () -> assertThat(dto.listOfAtomicInteger, hasSize(1)),
                () -> assertThat(dto.listOfAtomicInteger.get(0).get(), equalTo(1))

        );

    }

    @Getter
    static class DtoWithWildcardList {
        @CollectionRule(
                collectionClass = Vector.class,
                element = @Entry(stringRule = @StringRule))
        List<?> wildCardList;
    }

    @Getter
    static class DtoWithRawList {
        @CollectionRule(
                collectionClass = Vector.class)
        List rawList;
    }

    @Getter
    static class DtoWithListOfCollections {
        @CollectionRule
        @CollectionRule
        List<Set<String>> listOfSet;

        @CollectionRule(
                collectionClass = LinkedList.class)
        List<System> listOfString;
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Wildcard generic type")
    void wildcardGenericType() {
        DtoGeneratorException dtoGeneratorException =
                assertThrows(DtoGeneratorException.class, () -> DtoGenerator.builder(DtoWithWildcardList.class).build());

        String errorMessage = dtoGeneratorException.getMessage();

        assertAll(
                () -> assertThat(errorMessage, containsString("'1' error(s)")),
                () -> assertThat(errorMessage, containsString("Next type must have single generic type: 'java.util.List<?>'"))
        );
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Raw list")
    void rawList() {
        DtoGeneratorException dtoGeneratorException = assertThrows(DtoGeneratorException.class,
                () -> DtoGenerator.builder(DtoWithRawList.class).build());

        String errorMessage = dtoGeneratorException.getMessage();

        assertAll(
                () -> assertThat(errorMessage, containsString("'1' error(s)")),
                () -> assertThat(errorMessage, containsString("Next type must have single generic type: 'java.util.List'"))
        );
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("List Of Collection")
    void listOfCollection() {
        AtomicReference<DtoGenerator<DtoWithListOfCollections>> generator = new AtomicReference<>();

        DtoGeneratorException dtoGeneratorException = assertThrows(DtoGeneratorException.class, () ->
                generator.set(DtoGenerator.builder(DtoWithListOfCollections.class).build())
        );

        String errorsDetails = dtoGeneratorException.getMessage();

        assertAll(
                () -> assertThat(errorsDetails, containsString("'2' error(s)")),
                () -> assertThat(errorsDetails, containsString("is repeating for field")),
                () -> assertThat(errorsDetails, containsString("failed to select @Rules annotation by type"))
        );
    }

    static class Dto3 {

        @CollectionRule(element = @Entry(numberRule =
        @NumberRule))
        List<String> some;

    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Wrong Element Rule Annotation")
    void wrongElementRuleAnnotation() {

        DtoGeneratorException dtoGeneratorException =
                assertThrows(DtoGeneratorException.class, () -> DtoGenerator.builder(Dto3.class).build());

        String errorMessage = dtoGeneratorException.getMessage();

        assertThat(errorMessage, containsString("'1' error(s)"));
        assertThat(errorMessage,
                containsString("'class java.lang.String' does not match to rules annotation: '@NumberRule'"));
    }

    @Getter
    @NoArgsConstructor
    static class DtoSet {


        @CollectionRule
        Set<Integer> numbers;

        @CollectionRule(
                collectionClass = LinkedHashSet.class)
        Set<Integer> linkedHashSet;

        @CollectionRule(
                collectionClass = TreeSet.class)
        TreeSet<Integer> treeSet;

        @CollectionRule
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
