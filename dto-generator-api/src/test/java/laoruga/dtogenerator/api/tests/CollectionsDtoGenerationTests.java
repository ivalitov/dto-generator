package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import laoruga.dtogenerator.api.markup.rules.StringRules;
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

@DisplayName("Basic Type Generators Tests")
@Epic("LIST_RULES")
public class CollectionsDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @ListRules(listClass = LinkedList.class)
        @StringRules
        private List<String> linkedListOfStrings;

        @ListRules(listClass = Vector.class)
        @StringRules
        private List<String> vectorOfStrings;

        @ListRules
        @StringRules
        private ArrayList<String> arrayListOfStringsImplicit;

        @ListRules(listClass = LinkedList.class)
        @StringRules
        private LinkedList<String> linkedListOfStringsImplicit;
    }


    @Test
    @Feature("LIST_RULES")
    @DisplayName("List Of Integer Generation (default rules params)")
    public void listOfIntegerWithDefaultRulesPrams() {
        ClientDto dto = DtoGenerator.builder().build().generateDto(ClientDto.class);

        assertNotNull(dto);
        List<Integer> numbers = dto.getLinkedListExplicitRules();
        assertThat(numbers.size(), both(
                greaterThanOrEqualTo(ListRules.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRules.DEFAULT_MAX_SIZE)));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
        }
    }


    @Test
    @Feature("LIST_RULES")
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
    @Feature("LIST_RULES")
    @DisplayName("List Of Strings Generation (explicit rules params)")
    public void listOfStingsWithExplicitRulesPrams() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);

        assertNotNull(dto);

        Consumer<List<String>> assertListOfStrings = (list) -> {
            assertThat(list.size(), both(
                    greaterThanOrEqualTo(ListRules.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRules.DEFAULT_MAX_SIZE)));
            assertThat(list, everyItem(
                    notNullValue()
            ));

            for (String str : list) {
                assertThat(str.length(), both(
                        greaterThanOrEqualTo(StringRules.DEFAULT_MIN_SYMBOLS_NUMBER))
                        .and(lessThanOrEqualTo(StringRules.DEFAULT_MAX_SYMBOLS_NUMBER)));
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
        private DtoWithWildcardList() {
        }

        @ListRules
        @StringRules
        List<?> wildCardList;
    }

    @Getter
    static class DtoWithRawList {
        @ListRules()
        List rawList;
    }

    @Getter
    static class DtoWithCustomTypeList {
        @ListRules(listClass = LinkedList.class)
        List<Foo> customTypeList;

        class Foo {
        }
    }

    @Getter
    static class DtoWithListOfCollections {
        @ListRules()
        List<Set<String>> customTypeList;
    }

    @Feature("NEGATIVE_TESTS")
    static class NegativeTests {

        @Test
        @DisplayName("Unexpected wildcard generic type")
        public void listOfIntegerWithDefaultRulesPrams() throws NoSuchFieldException, IllegalAccessException {
            DtoGenerator generator = DtoGenerator.builder().build();
            assertThrows(DtoGeneratorException.class,
                    () -> generator.generateDto(DtoWithWildcardList.class));

            Map<String, Exception> errorsMap = getErrorsMap(generator);

            assertEquals(1, errorsMap.size());
            assertTrue(errorsMap.containsKey("wildCardList"));
            assertThat(errorsMap.get("wildCardList").getMessage(), stringContainsInOrder("Can't generate wildcard type"));
        }

    }

}
