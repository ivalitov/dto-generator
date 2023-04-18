package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.StringRule;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 04.04.2023
 */

@Epic("CUSTOM_RULES")
@Feature("CUSTOM_GENERATOR_WITH_ARGS")
public class CustomGeneratorWIthArgsTests {

    final static String[] DEFAULT_ARGS = {"horn", "frogs"};
    final static String[] DEFAULT_NESTED_ARGS = {"flower", "fog"};

    static class Dto {

        @StringRule(words = {"Leila", "Hazel"})
        String witchName;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrew;

        @CustomRule(generatorClass = WitchesBrewGenerator.class, args = {"horn", "frogs"})
        WitchesBrew witchesBrewWithArgs;

        @NestedDtoRule
        NestedDto nestedDto;

    }

    static class NestedDto {

        @StringRule(words = {"Grumpy", "Monalisa"})
        String witchName;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrew;

        @CustomRule(generatorClass = WitchesBrewGenerator.class, args = {"flower", "fog"})
        WitchesBrew witchesBrewWithArgs;

    }

    static class WitchesBrew {

        String[] ingredients;

        public WitchesBrew(String[] ingredients) {
            this.ingredients = ingredients;
        }
    }

    static class WitchesBrewGenerator implements CustomGeneratorArgs<WitchesBrew> {

        String[] ingredients;

        @Override
        public WitchesBrew generate() {
            return new WitchesBrew(ingredients);
        }

        @Override
        public void setArgs(String... ingredients) {
            this.ingredients = ingredients;
        }
    }

    static class WitchesBrewGenerator_2 implements CustomGeneratorArgs<WitchesBrew> {

        String[] ingredients;

        @Override
        public WitchesBrew generate() {
            return new WitchesBrew(
                    Arrays.stream(ingredients).map(i -> "SECOND " + i).toArray(String[]::new)
            );
        }

        @Override
        public void setArgs(String... ingredients) {
            this.ingredients = ingredients;
        }
    }

    @Test
    void customGeneratorWithArgsAsIs() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew.ingredients, emptyArray()),
                () -> assertThat(dto.witchesBrewWithArgs.ingredients, equalTo(DEFAULT_ARGS)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, emptyArray()),
                () -> assertThat(dto.nestedDto.witchesBrewWithArgs.ingredients, equalTo(DEFAULT_NESTED_ARGS))
        );
    }

    /*
     * Args overriding
     */

    @Test
    @DisplayName("Args from annotation overridden by generator TYPE")
    void customGeneratorWithArgsOverriddenByType() {
        final String[] ingredients = {"honey", "moon"};

        Dto dto = DtoGenerator.builder(Dto.class)
                .setGeneratorArgs(WitchesBrewGenerator.class, ingredients)
                .build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.witchesBrewWithArgs.ingredients, equalTo(ingredients)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.nestedDto.witchesBrewWithArgs.ingredients, equalTo(ingredients))
        );
    }

    @Test
    @DisplayName("Args from annotation overridden by FIELD NAME")
    void customGeneratorWithArgsOverriddenByField() {

        final String[] ingredients_1 = {"one", "two"};
        final String[] ingredients_2 = {"three", "four"};

        // args set for root dto only

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setGeneratorArgs("witchesBrew", ingredients_1)
                .setGeneratorArgs("witchesBrewWithArgs", ingredients_2);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients_1)),
                () -> assertThat(dto.witchesBrewWithArgs.ingredients, equalTo(ingredients_2)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, emptyArray()),
                () -> assertThat(dto.nestedDto.witchesBrewWithArgs.ingredients, equalTo(DEFAULT_NESTED_ARGS))
        );

        // args set for root dto and nested dto

        final String[] ingredients_3 = {"five", "six"};

        builder
                .setGeneratorArgs("nestedDto.witchesBrew", ingredients_2)
                .setGeneratorArgs("nestedDto.witchesBrewWithArgs", ingredients_3);

        Dto dto_2 = builder.build().generateDto();


        assertAll(
                () -> assertThat(dto_2.witchName, notNullValue()),
                () -> assertThat(dto_2.witchesBrew.ingredients, equalTo(ingredients_1)),
                () -> assertThat(dto_2.witchesBrewWithArgs.ingredients, equalTo(ingredients_2)),

                () -> assertThat(dto_2.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto_2.nestedDto.witchesBrew.ingredients, equalTo(ingredients_2)),
                () -> assertThat(dto_2.nestedDto.witchesBrewWithArgs.ingredients, equalTo(ingredients_3))
        );
    }

    @Test
    @DisplayName("Args from annotation overridden both by TYPE and FIELD NAME")
    void customGeneratorWithArgsOverriddenByTypeAndField() {

        final String[] ingredients_1 = {"one", "two"};
        final String[] ingredients_2 = {"three", "four"};
        final String[] ingredients_3 = {"five", "six"};

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setGeneratorArgs(WitchesBrewGenerator.class, ingredients_1);

        builder
                .setGeneratorArgs("witchesBrew", ingredients_2)
                .setGeneratorArgs("nestedDto.witchesBrewWithArgs", ingredients_3);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients_2)),
                () -> assertThat(dto.witchesBrewWithArgs.ingredients, equalTo(ingredients_1)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients_1)),
                () -> assertThat(dto.nestedDto.witchesBrewWithArgs.ingredients, equalTo(ingredients_3))
        );
    }

    @TestFactory
    Stream<DynamicTest> customGeneratorWithArgsBothGeneratorAndArgsOverriding() {

        final String constant = "kindness";

        final String[] args = new String[]{"SECOND " + constant};

        Consumer<Dto> assertions = dto ->
                assertAll(
                        () -> assertThat(dto.witchName, notNullValue()),
                        () -> assertThat(dto.witchesBrew.ingredients, equalTo(args)),
                        () -> assertThat(dto.witchesBrewWithArgs.ingredients, equalTo(args)),

                        () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                        () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(args)),
                        () -> assertThat(dto.nestedDto.witchesBrewWithArgs.ingredients, equalTo(args))
                );

        return Stream.of(

                DynamicTest.dynamicTest("Args set for TYPE with 'setGenerator(..)' method",
                        () -> assertions.accept(
                                DtoGenerator.builder(Dto.class)
                                        .setGenerator(WitchesBrew.class, new WitchesBrewGenerator_2(), constant)
                                        .build().generateDto()
                        )),

                DynamicTest.dynamicTest("Args set for FIELD NAME with 'setGenerator(..)' method",
                        () -> assertions.accept(
                                DtoGenerator.builder(Dto.class)
                                        .setGenerator("witchesBrew", new WitchesBrewGenerator_2(), constant)
                                        .setGenerator("witchesBrewWithArgs", new WitchesBrewGenerator_2(), constant)
                                        .setGenerator("nestedDto.witchesBrew", new WitchesBrewGenerator_2(), constant)
                                        .setGenerator("nestedDto.witchesBrewWithArgs", new WitchesBrewGenerator_2(),constant)
                                        .build().generateDto()
                        )),

                DynamicTest.dynamicTest("Args set for TYPE with 'setGeneratorArgs(..)' method for generator overridden by TYPE",
                        () -> assertions.accept(
                                DtoGenerator.builder(Dto.class)
                                        .setGeneratorArgs(WitchesBrewGenerator_2.class, constant)
                                        .setGenerator(WitchesBrew.class, new WitchesBrewGenerator_2())
                                        .build().generateDto()
                        )),

                DynamicTest.dynamicTest("Args set for TYPE with 'setGeneratorArgs(..)' method for generator overridden by FIELD NAME",
                        () -> assertions.accept(
                                DtoGenerator.builder(Dto.class)
                                        .setGenerator("witchesBrew", new WitchesBrewGenerator_2())
                                        .setGenerator("witchesBrewWithArgs", new WitchesBrewGenerator_2())
                                        .setGenerator("nestedDto.witchesBrew", new WitchesBrewGenerator_2())
                                        .setGenerator("nestedDto.witchesBrewWithArgs", new WitchesBrewGenerator_2())
                                        .setGeneratorArgs(WitchesBrewGenerator_2.class, constant)
                                        .build().generateDto()
                        )),

                DynamicTest.dynamicTest("Args set for FIELD NAME with 'setGeneratorArgs(..)' method for generator overridden by FIELD NAME",
                        () -> assertions.accept(
                                DtoGenerator.builder(Dto.class)
                                        .setGeneratorArgs("witchesBrew", constant)
                                        .setGeneratorArgs("witchesBrewWithArgs", constant)
                                        .setGenerator(WitchesBrew.class, new WitchesBrewGenerator_2())
                                        .setGeneratorArgs("nestedDto.witchesBrew", constant)
                                        .setGeneratorArgs("nestedDto.witchesBrewWithArgs", constant)
                                        .build().generateDto()
                        )),

                DynamicTest.dynamicTest("Args set for FIELD NAME with 'setGeneratorArgs(..)' method for generator overridden by FIELD NAME",
                        () -> assertions.accept(
                                DtoGenerator.builder(Dto.class)
                                        .setGeneratorArgs("witchesBrew", constant)
                                        .setGeneratorArgs("witchesBrewWithArgs", constant)
                                        .setGeneratorArgs("nestedDto.witchesBrew", constant)
                                        .setGeneratorArgs("nestedDto.witchesBrewWithArgs", constant)
                                        .setGenerator("witchesBrew", new WitchesBrewGenerator_2())
                                        .setGenerator("witchesBrewWithArgs", new WitchesBrewGenerator_2())
                                        .setGenerator("nestedDto.witchesBrew", new WitchesBrewGenerator_2())
                                        .setGenerator("nestedDto.witchesBrewWithArgs", new WitchesBrewGenerator_2())
                                        .build().generateDto()
                        ))
        );
    }

}
