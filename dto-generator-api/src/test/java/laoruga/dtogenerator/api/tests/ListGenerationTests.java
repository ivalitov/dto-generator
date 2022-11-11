package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.ListRule;
import laoruga.dtogenerator.api.markup.rules.SetRule;
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
                greaterThanOrEqualTo(ListRule.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRule.DEFAULT_MAX_SIZE)));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)));
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
                    greaterThanOrEqualTo(ListRule.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRule.DEFAULT_MAX_SIZE)));
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

        Map<String, Exception> errorsMap = getErrorsMap(generator);

        assertEquals(1, errorsMap.size());
        assertTrue(errorsMap.containsKey("wildCardList"));
        assertThat(errorsMap.get("wildCardList").getMessage(), stringContainsInOrder("Can't generate wildcard type"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("Raw list")
    void rawList() {
        DtoGenerator<DtoWithRawList> generator = DtoGenerator.builder(DtoWithRawList.class).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = getErrorsMap(generator);

        assertEquals(1, errorsMap.size());
        assertTrue(errorsMap.containsKey("rawList"));
        assertThat(errorsMap.get("rawList").getMessage(), stringContainsInOrder("Can't generate raw type"));
    }

    @Test
    @Feature("NEGATIVE_TESTS")
    @DisplayName("List Of Collection")
    // TODO fix
    void listOfCollection() {
        DtoGenerator<DtoWithListOfCollections> generator = DtoGenerator.builder(DtoWithListOfCollections.class).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);

        Map<String, Exception> errorsMap = getErrorsMap(generator);

        assertEquals(2, errorsMap.size());
        assertTrue(errorsMap.containsKey("listOfSet"));
        assertThat(errorsMap.get("listOfSet").getCause().getMessage(),
                stringContainsInOrder("Found '2' @CollectionRule annotations for various collection types"));
        assertTrue(errorsMap.containsKey("listOfString"));
        assertThat(errorsMap.get("listOfString").getCause().getMessage(),
                stringContainsInOrder("Missed @Rule annotation for item of collection"));

    }

}
