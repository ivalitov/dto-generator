package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import laoruga.dtogenerator.api.tests.data.dtoclient.ClientDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import static laoruga.dtogenerator.api.tests.util.TestUtils.getErrorsMap;
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
public class ListGenerationTests {

    @Getter
    @NoArgsConstructor
    static class DtoList {

        @ListRules(listClass = LinkedList.class)
        @StringRule
        private List<String> linkedListOfStrings;

        @ListRules(listClass = Vector.class)
        @StringRule
        private List<String> vectorOfStrings;

        @ListRules
        @StringRule
        private ArrayList<String> arrayListOfStringsImplicit;

        @ListRules(listClass = LinkedList.class)
        @StringRule
        private LinkedList<String> linkedListOfStringsImplicit;
    }

    @Test
    @DisplayName("List Of Integer Generation (default rules params)")
    public void listOfIntegerWithDefaultRulesPrams() {
        ClientDto dto = DtoGenerator.builder().build().generateDto(ClientDto.class);

        assertNotNull(dto);
        List<Integer> numbers = dto.getArrayListIntegerRules();
        assertThat(numbers.size(), both(
                greaterThanOrEqualTo(ListRules.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRules.DEFAULT_MAX_SIZE)));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)));
        }
    }


    @Test
    @DisplayName("List Of Integer Generation (explicit rules params)")
    public void listOfIntegerWithExplicitRulesPrams() {
        ClientDto dto = DtoGenerator.builder().build().generateDto(ClientDto.class);

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
    public void listOfStingsWithExplicitRulesPrams() {
        DtoList dto = DtoGenerator.builder().build().generateDto(DtoList.class);

        assertNotNull(dto);

        Consumer<List<String>> assertListOfStrings = (list) -> {
            assertThat(list.size(), both(
                    greaterThanOrEqualTo(ListRules.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRules.DEFAULT_MAX_SIZE)));
            assertThat(list, everyItem(
                    notNullValue()
            ));

            for (String str : list) {
                assertThat(str.length(), both(
                        greaterThanOrEqualTo(StringRule.DEFAULT_MIN_SYMBOLS_NUMBER))
                        .and(lessThanOrEqualTo(StringRule.DEFAULT_MAX_SYMBOLS_NUMBER)));
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
        @ListRules
        @StringRule
        List<?> wildCardList;
    }

    @Getter
    static class DtoWithRawList {
        @ListRules
        @IntegerRule
        List rawList;
    }

    @Getter
    static class DtoWithListOfCollections {
        @ListRules()
        List<Set<String>> listOfSet;

        @ListRules(listClass = LinkedList.class)
        List<String> listOfString;
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Wildcard generic type")
    public void wildcardGenericType() {
        DtoGenerator generator = DtoGenerator.builder().build();
        assertThrows(DtoGeneratorException.class,
                () -> generator.generateDto(DtoWithWildcardList.class));

        Map<String, Exception> errorsMap = getErrorsMap(generator);

        assertEquals(1, errorsMap.size());
        assertTrue(errorsMap.containsKey("wildCardList"));
        assertThat(errorsMap.get("wildCardList").getMessage(), stringContainsInOrder("Can't generate wildcard type"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Raw list")
    public void rawList() {
        DtoGenerator generator = DtoGenerator.builder().build();
        assertThrows(DtoGeneratorException.class,
                () -> generator.generateDto(DtoWithRawList.class));

        Map<String, Exception> errorsMap = getErrorsMap(generator);

        assertEquals(1, errorsMap.size());
        assertTrue(errorsMap.containsKey("rawList"));
        assertThat(errorsMap.get("rawList").getMessage(), stringContainsInOrder("Can't generate raw type"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("List Of Collection")
    public void listOfCollection() {
        DtoGenerator generator = DtoGenerator.builder().build();
        assertThrows(DtoGeneratorException.class,
                () -> generator.generateDto(DtoWithListOfCollections.class));

        Map<String, Exception> errorsMap = getErrorsMap(generator);
        final String ERROR_PART = "There is also generation rules annotation expected";

        assertEquals(2, errorsMap.size());
        assertTrue(errorsMap.containsKey("listOfSet"));
        assertThat(errorsMap.get("listOfSet").getMessage(), stringContainsInOrder(ERROR_PART));
        assertTrue(errorsMap.containsKey("listOfString"));
        assertThat(errorsMap.get("listOfString").getMessage(), stringContainsInOrder(ERROR_PART));
    }

}
