package org.laoruga.dtogenerator;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.laoruga.dtogenerator.constants.BoundaryConfig.*;

/**
 * @author Il'dar Valitov
 * Created on 22.11.2022
 */

@DisplayName("DtoGeneratorBuilder tests")
@Epic("UNIT_TESTS")
@Feature("DTO_GENERATOR_BUILDER")
class DtoGeneratorBuilderTests {

    @NoArgsConstructor
    static class Dto {
        String string;
    }

    @Test
    @DisplayName("Attempt to overwrite remark for field")
    void tryToOverwriteRemarkForField() {
        DtoGeneratorException e = assertThrows(DtoGeneratorException.class,
                () -> DtoGenerator.builder(Dto.class)
                        .setBoundaryConfig("string", MAX_VALUE)
                        .setBoundaryConfig("string", MIN_VALUE));
        assertThat(e.getMessage(), containsString("Attempt to overwrite remark"));
    }

    @Test
    @DisplayName("Attempt to overwrite remark for all fields")
    void tryToOverwriteRemarkForAllFields() {
        DtoGeneratorException e = assertThrows(DtoGeneratorException.class,
                () -> DtoGenerator.builder(Dto.class)
                        .setBoundaryConfig(RANDOM_VALUE)
                        .setBoundaryConfig(NULL_VALUE));
        assertThat(e.getMessage(), containsString("Attempt to overwrite remark for all fields"));
    }

}
