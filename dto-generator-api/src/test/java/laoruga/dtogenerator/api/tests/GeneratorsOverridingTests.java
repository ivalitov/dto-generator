package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 02.07.2022
 */
@DisplayName("Overriding of generators")
@Epic("GENERATORS_OVERRIDING")
public class GeneratorsOverridingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @IntegerRule
        Integer integer;

        @StringRule
        String string;

        @NestedDtoRules
        InnerDto innerDto;
    }

    @Getter
    @NoArgsConstructor
    static class InnerDto {
        @IntegerRule
        Integer innerInteger;

        @StringRule
        String innerString;
    }

    static class NumberGenerator implements ICustomGeneratorArgs<Integer> {
        int generated;
        @Override
        public NumberGenerator setArgs(String... args) {
            generated = Integer.parseInt(args[0]);
            return this;
        }
        @Override
        public Integer generate() {
            return generated;
        }
    }

    @Test
    @DisplayName("Basic generator overridden")
    public void basicGeneratorOverridden() {
        Dto dto = DtoGenerator.builder()
                .setBasicGenerator(IntegerRule.class, () -> new NumberGenerator().setArgs("123")).build()
                .generateDto(Dto.class);

        assertAll(
                () -> assertThat(dto.getInteger(), equalTo(123)),
                () -> assertThat(dto.getString(), notNullValue()),
                () -> assertThat(dto.getInnerDto().getInnerInteger(), equalTo(123)),
                () -> assertThat(dto.getInnerDto().getInnerString(), notNullValue())
        );
    }

    @Test
    @DisplayName("Field generator overridden")
    public void fieldGeneratorOverridden() {
        Dto dto = DtoGenerator.builder()
                .setFieldGenerator("integer", () -> new NumberGenerator().setArgs("123"))
                .setFieldGenerator("innerDto.innerInteger", () -> new NumberGenerator().setArgs("456"))
                .build().generateDto(Dto.class);

        assertAll(
                () -> assertThat(dto.getInteger(), equalTo(123)),
                () -> assertThat(dto.getString(), notNullValue()),
                () -> assertThat(dto.getInnerDto().getInnerInteger(), equalTo(456)),
                () -> assertThat(dto.getInnerDto().getInnerString(), notNullValue())
        );
    }

}
