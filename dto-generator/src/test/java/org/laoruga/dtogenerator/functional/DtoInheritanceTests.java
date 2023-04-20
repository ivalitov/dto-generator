package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.IntegralRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.generator.config.dto.StringConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
@DisplayName("DTO Inheritance")
@Epic("DTO_INHERITANCE")
@Slf4j
class DtoInheritanceTests {

    @Getter
    static abstract class DtoAbstract {

        @StringRule(words = "stringAbstract")
        String stringAbstract;
    }

    @Getter
    static class DtoSuper extends DtoAbstract {

        @StringRule(words = "stringSuper")
        String stringSuper;
    }

    @NoArgsConstructor
    @Getter
    static class Dto extends DtoSuper {

        @StringRule(words = "string")
        String string;

        @NestedDtoRule
        DtoNested dtoNested;
    }

    @Getter
    static class DtoNestedSuper {

        @StringRule(words = "stringNestedSuper")
        String stringNestedSuper;
    }

    @Getter
    static class DtoNested extends DtoNestedSuper {

        @StringRule(words = "stringNested")
        String stringNested;

        @IntegralRule(minInt = 1, maxInt = 1)
        Integer integerNested;
    }


    @Test
    @DisplayName("DTO With Inheritance")
    void dtoWithInheritance() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .setGeneratorConfig("stringAbstract", StringConfig.builder().words(new String[]{"stringAbstract_2"}).build())
                .setGeneratorConfig("dtoNested.stringNested", StringConfig.builder().words(new String[]{"stringNested_2"}).build())
                .build().generateDto();
        assertAll(
                () -> assertThat(dto.getString(), equalTo("string")),
                () -> assertThat(dto.getStringSuper(), equalTo("stringSuper")),
                () -> assertThat(dto.getStringAbstract(), equalTo("stringAbstract_2")),
                () -> assertThat(dto.getDtoNested().getStringNested(), equalTo("stringNested_2")),
                () -> assertThat(dto.getDtoNested().getIntegerNested(), equalTo(1)),
                () -> assertThat(dto.getDtoNested().getStringNestedSuper(), equalTo("stringNestedSuper"))
        );

    }


}
