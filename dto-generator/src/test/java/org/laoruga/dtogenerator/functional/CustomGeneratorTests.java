package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.rules.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 04.04.2023
 */

@Epic("CUSTOM_RULES")
@Feature("CUSTOM_GENERATOR_WITH_ARGS")
public class CustomGeneratorTests {

    final static String[] INGREDIENTS = {"horn", "frogs"};
    final static String[] INGREDIENTS_2 = {"wind", "sand"};
    final static String[] INGREDIENTS_3 = {"soda", "water"};

    final static WitchesBrew brew = new WitchesBrew(INGREDIENTS);
    final static WitchesBrew brew_2 = new WitchesBrew(INGREDIENTS_2);
    final static WitchesBrew brew_3 = new WitchesBrew(INGREDIENTS_3);

    static class Dto {

        @StringRule(words = {"Leila", "Hazel"})
        String witchName;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrew;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrewSecond;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator.class)))
        List<WitchesBrew> witchesBrewList;

        @ArrayRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator.class)))
        WitchesBrew[] witchesBrewArray;

        @MapRule(maxSize = 1,
                key = @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator.class)),
                value = @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator_2.class))
        )
        Map<WitchesBrew, WitchesBrew> witchesBrewMap;

        @NestedDtoRule
        NestedDto nestedDto;
    }

    static class NestedDto {

        @StringRule(words = {"Grumpy", "Monalisa"})
        String witchName;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrew;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrewSecond;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator.class)))
        List<WitchesBrew> witchesBrewList;

        @ArrayRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator.class)))
        WitchesBrew[] witchesBrewArray;

        @MapRule(maxSize = 1,
                key = @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator.class)),
                value = @Entry(customRule = @CustomRule(generatorClass = WitchesBrewGenerator_2.class))
        )
        Map<WitchesBrew, WitchesBrew> witchesBrewMap;


    }

    @EqualsAndHashCode
    static class WitchesBrew {

        String[] ingredients;

        public WitchesBrew(String[] ingredients) {
            this.ingredients = ingredients;
        }
    }

    static class WitchesBrewGenerator implements CustomGenerator<WitchesBrew> {

        @Override
        public WitchesBrew generate() {
            return new WitchesBrew(INGREDIENTS);
        }

    }

    static class WitchesBrewGenerator_2 implements CustomGenerator<WitchesBrew> {

        @Override
        public WitchesBrew generate() {
            return new WitchesBrew(INGREDIENTS_2);
        }

    }

    static class WitchesBrewGenerator_3 implements CustomGenerator<WitchesBrew> {

        @Override
        public WitchesBrew generate() {
            return new WitchesBrew(INGREDIENTS_3);
        }

    }

    @Test
    void customGeneratorAsIs() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew, equalTo(brew)),
                () -> assertThat(dto.witchesBrewSecond, equalTo(brew)),
                () -> assertThat(dto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),
                () -> assertThat(Arrays.asList(dto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),

                // nested
                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew, equalTo(brew)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond, equalTo(brew)),
                () -> assertThat(dto.nestedDto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),
                () -> assertThat(Arrays.asList(dto.nestedDto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                )
        );
    }

    /*
     * Args overriding
     */

    @Test
    void customGeneratorOverriddenByType() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .setGenerator(WitchesBrew.class, new WitchesBrewGenerator_2())
                .build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew, equalTo(brew_2)),
                () -> assertThat(dto.witchesBrewSecond, equalTo(brew_2)),
                () -> assertThat(dto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                ),
                () -> assertThat(Arrays.asList(dto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                ),

                // nested
                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew, equalTo(brew_2)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond, equalTo(brew_2)),
                () -> assertThat(dto.nestedDto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                ),
                () -> assertThat(Arrays.asList(dto.nestedDto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                )
        );
    }

    @Test
    void customGeneratorOverriddenByField() {

        // generator set for root dto only

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setGenerator("witchesBrew", new WitchesBrewGenerator_2())
                .setGenerator("witchesBrewSecond", new WitchesBrewGenerator_2());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew, equalTo(brew_2)),
                () -> assertThat(dto.witchesBrewSecond, equalTo(brew_2)),
                () -> assertThat(dto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),
                () -> assertThat(Arrays.asList(dto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew, equalTo(brew)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond, equalTo(brew)),
                () -> assertThat(dto.nestedDto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),
                () -> assertThat(Arrays.asList(dto.nestedDto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                )
        );

        // generator set for root dto and nested dto

        builder
                .setGenerator("nestedDto.witchesBrew", new WitchesBrewGenerator_2())
                .setGenerator("nestedDto.witchesBrewSecond", new WitchesBrewGenerator_2());

        Dto dto_2 = builder.build().generateDto();


        assertAll(
                () -> assertThat(dto_2.witchName, notNullValue()),
                () -> assertThat(dto_2.witchesBrew, equalTo(brew_2)),
                () -> assertThat(dto_2.witchesBrewSecond, equalTo(brew_2)),
                () -> assertThat(dto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),
                () -> assertThat(Arrays.asList(dto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),

                () -> assertThat(dto_2.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto_2.nestedDto.witchesBrew, equalTo(brew_2)),
                () -> assertThat(dto_2.nestedDto.witchesBrewSecond, equalTo(brew_2)),
                () -> assertThat(dto.nestedDto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                ),
                () -> assertThat(Arrays.asList(dto.nestedDto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew)))
                )
        );
    }

    @Test
    void customGeneratorOverriddenByTypeAndField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setGenerator(WitchesBrew.class, new WitchesBrewGenerator_2());

        builder
                .setGenerator("witchesBrew", new WitchesBrewGenerator_3())
                .setGenerator("nestedDto.witchesBrewSecond", new WitchesBrewGenerator_3());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchesBrew, equalTo(brew_3)),
                () -> assertThat(dto.witchesBrewSecond, equalTo(brew_2)),
                () -> assertThat(dto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                ),
                () -> assertThat(Arrays.asList(dto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                ),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchesBrew, equalTo(brew_2)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond, equalTo(brew_3)),
                () -> assertThat(dto.nestedDto.witchesBrewList,
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                ),
                () -> assertThat(Arrays.asList(dto.nestedDto.witchesBrewArray),
                        both(hasSize(10)).and(everyItem(equalTo(brew_2)))
                )
        );
    }

}
