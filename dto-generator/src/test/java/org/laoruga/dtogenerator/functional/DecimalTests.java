package org.laoruga.dtogenerator.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.DecimalRule;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 13.03.2023
 */

//TODO improve coverage
@ExtendWith(Extensions.RestoreStaticConfig.class)
public class DecimalTests {

    static class Dto {

        @DecimalRule
        Double doubleObject;

        @DecimalRule(minDouble = -5)
        double doublePrimitive;

        @DecimalRule
        Float floatObject;

        @DecimalRule(maxFloat = 123F)
        float floatPrimitive;

        @DecimalRule
        BigDecimal bigDecimal;

    }

    @Test
    public void annotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, notNullValue()),
                () -> assertThat(dto.doublePrimitive, greaterThanOrEqualTo(-5D)),
                () -> assertThat(dto.floatObject, notNullValue()),
                () -> assertThat(dto.floatPrimitive, lessThanOrEqualTo(123F)),
                () -> assertThat(dto.bigDecimal, notNullValue())
        );

    }

}
