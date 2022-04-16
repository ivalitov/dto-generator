package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    public void smokeTest() {

        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getIntDefaultRules()),
                () -> assertTrue(dto.getIntDefaultRules() > 0)
        );
    }





    @DisplayName("Integer Generator")
    static class IntegerGenerator {

    }

}
