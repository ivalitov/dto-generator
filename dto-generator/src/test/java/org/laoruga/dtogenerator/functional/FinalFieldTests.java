package org.laoruga.dtogenerator.functional;

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.StringRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 17.02.2023
 */
@DisplayName("Final Fields Generation Test")
public class FinalFieldTests {


    @Getter
    static class Dto {

        @StringRule
        final String finalString = "CONSTANT";

        @StringRule
        String string = "variable";

    }

    @Test
    @DisplayName("Do Not Generate Final Field")
    void doNotGenerateFinal() {

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.getFinalString(), equalTo("CONSTANT")),
                () -> assertThat(dto.getString(), not(equalTo("variable")))
        );

    }

    @Getter
    static class Dto2 {

        @StringRule
        static final String staticFinalString = "STATIC_CONSTANT";

        @StringRule
        static String staticString = "static_variable";

        @StringRule
        String string = "string";
    }

    @Test
    @DisplayName("Generate Static Field But Not Final")
    void staticFields() {

        Dto2 dto = DtoGenerator.builder(Dto2.class).build().generateDto();

        assertAll(
                () -> assertThat(Dto2.staticFinalString, equalTo("STATIC_CONSTANT")),
                () -> assertThat(Dto2.staticString, not(equalTo("static_variable"))),
                () -> assertThat(dto.getString(), not(equalTo("string")))
        );
    }

}
