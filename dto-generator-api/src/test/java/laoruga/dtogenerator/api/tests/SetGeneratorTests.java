package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.SetRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 31.05.2022
 */
@DisplayName("Set Type Generators Tests")
@Epic("SET_RULES")
public class SetGeneratorTests {

    @Getter
    @NoArgsConstructor
    static class DtoSet {
        @SetRules
        @IntegerRules
        Set<Integer> numbers;

        @SetRules(setClass = LinkedHashSet.class)
        @IntegerRules
        Set<Integer> linkedHashSet;

        @SetRules(setClass = TreeSet.class)
        @IntegerRules
        TreeSet<Integer> treeSet;

        @SetRules
        @IntegerRules
        HashSet<Integer> hashSet;
    }

    @Test
    @DisplayName("Set Of Strings Generation (default rules params)")
    public void listOfIntegerWithDefaultRulesPrams() {
        DtoSet dto = DtoGenerator.builder().build().generateDto(DtoSet.class);

        assertNotNull(dto);
        Set<Integer> numbers = dto.getNumbers();
        assertThat(numbers.size(), both(
                greaterThanOrEqualTo(SetRules.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(SetRules.DEFAULT_MAX_SIZE)));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
        }
    }

}
