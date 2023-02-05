package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.api.rules.SetRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientDto;
import org.laoruga.dtogenerator.rules.RulesInstance;

import java.util.*;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */
// TODO Needs to check and handle situation when item rules and generic are not same
// TODO Add feature - remarks for item generator (maybe already exists?)
@DisplayName("List Type Generators Tests")
@Epic("LIST_RULES")
class ListGenerationTests {

    @Getter
    @NoArgsConstructor
    static class DtoList {

        @ListRule(listClass = LinkedList.class)
        @StringRule
        private List<String> linkedListOfStrings;

        @ListRule(listClass = Vector.class)
        @StringRule
        private List<String> vectorOfStrings;

        @ListRule
        @StringRule
        private ArrayList<String> arrayListOfStringsImplicit;

        @ListRule(listClass = LinkedList.class)
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
                greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(lessThanOrEqualTo(RulesInstance.listRule.maxSize())));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue())));
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
                    greaterThanOrEqualTo(RulesInstance.listRule.minSize()))
                    .and(lessThanOrEqualTo(RulesInstance.listRule.maxSize())));
            assertThat(list, everyItem(
                    notNullValue()
            ));

            for (String str : list) {
                assertThat(str.length(), both(
                        greaterThanOrEqualTo(RulesInstance.stringRule.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.stringRule.maxLength())));
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
    static class DtoWithWildcardList {
        @ListRule
        @StringRule
        List<?> wildCardList;
    }

    @Getter
    static class DtoWithRawList {
        @ListRule
        @IntegerRule
        List rawList;
    }

    @Getter
    static class DtoWithListOfCollections {
        @ListRule()
        @SetRule()
        List<Set<String>> listOfSet;

        @ListRule(listClass = LinkedList.class)
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
        assertThat(errorsMap.get("wildCardList").getMessage(),
                equalTo("Next type must have single generic type: 'java.util.List<?>'"));
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
        assertThat(errorsMap.get("rawList").getMessage(),
                equalTo("Next type must have single generic type: 'java.util.List'"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("List Of Collection")
    void listOfCollection() {
        DtoGenerator<DtoWithListOfCollections> generator = DtoGenerator.builder(DtoWithListOfCollections.class).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = UtilsRoot.getErrorsMap(generator);

        assertEquals(2, errorsMap.size());
        assertTrue(errorsMap.containsKey("listOfSet"));
        assertThat(errorsMap.get("listOfSet").getCause().getMessage(),
                stringContainsInOrder("Found '2' @CollectionRule annotations for various collection types"));
        assertTrue(errorsMap.containsKey("listOfString"));
        assertThat(errorsMap.get("listOfString").getCause().getMessage(),
                stringContainsInOrder("Missed @Rule annotation for item of collection"));

    }

}
