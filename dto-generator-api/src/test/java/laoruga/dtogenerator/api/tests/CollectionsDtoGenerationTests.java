package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import laoruga.dtogenerator.api.tests.data.ClientInfoDto;
import laoruga.dtogenerator.api.tests.data.customgenerator.ClientInfoGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Basic Type Generators Tests")
public class CollectionsDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @ListRules()
        @CustomGenerator(generatorClass = ClientInfoGenerator.class)
        private List<ClientInfoDto> clients;

        @ListRules(listClass = LinkedList.class)
        @IntegerRules
        private List<Integer> numbers;

    }


    @Test
    @Feature("LIST_RULES")
    @DisplayName("Nested Dto Generation")
    public void simpleIntegerGeneration() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);

        assertNotNull(dto);
        List<Integer> numbers = dto.getNumbers();
        assertThat(numbers.size(), both(
                greaterThanOrEqualTo(ListRules.DEFAULT_MIN_SIZE)).and(lessThanOrEqualTo(ListRules.DEFAULT_MAX_SIZE)));
        for (Integer number : numbers) {
            assertThat(number, both(
                    greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
        }

    }

}
