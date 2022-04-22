package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Basic Type Generators Tests")
public class BasitTypeGeneratorsTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @IntegerRules()
        private Integer intDefaultRules;
        @IntegerRules(minValue = 99999999)
        private Integer intLeftBound;
        @IntegerRules(maxValue = 100)
        private int intRightBound;
        @IntegerRules(minValue = -100, maxValue = 0)
        private int intLeftAndRightBounds;
        private int intPrimitiveDefault;
        private int intPrimitive = 999;

    }

    @Test
    public void simpleGeneration() {

        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);

        assertNotNull(dto);
        assertAll(
                () ->  assertThat(dto.getIntDefaultRules(), both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(999999999))),
                () ->  assertThat(dto.getIntLeftBound(), greaterThanOrEqualTo(99999999)),
                () ->  assertThat(dto.getIntRightBound(), both(greaterThanOrEqualTo(0)).and(lessThanOrEqualTo(100))),
                () ->  assertThat(dto.getIntLeftAndRightBounds(), both(greaterThanOrEqualTo(-100)).and(lessThanOrEqualTo(0))),
                () ->  assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () ->  assertThat(dto.getIntPrimitive(), equalTo(999))

        );
    }





    @DisplayName("Integer Generator")
    static class IntegerGenerator {

    }

}
