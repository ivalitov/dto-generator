package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.SetRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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
        @SetRule
        @IntegerRule
        Set<Integer> numbers;

        @SetRule(setClass = LinkedHashSet.class)
        @IntegerRule
        Set<Integer> linkedHashSet;

        @SetRule(setClass = TreeSet.class)
        @IntegerRule
        TreeSet<Integer> treeSet;

        @SetRule
        @IntegerRule
        HashSet<Integer> hashSet;
    }

    @Test
    @DisplayName("Set Of Integers Generation (default rules params)")
    public void listOfIntegerWithDefaultRulesPrams() {
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
                greaterThanOrEqualTo(SetRule.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(SetRule.DEFAULT_MAX_SIZE)));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)));
        }
    }

}
