package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.tests.BasitTypeGeneratorsTests.simpleIntegerGenerationAssertions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Basic Type Generators Tests")
public class NestedDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @IntegerRules()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private BasitTypeGeneratorsTests.DtoInteger dtoNested;
    }

    @Test
    @Feature("INTEGER_RULES")
    @DisplayName("Nested Dto Generation")
    public void simpleIntegerGeneration() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
        simpleIntegerGenerationAssertions(dto.getDtoNested());

    }

}
