package org.laoruga.dtogenerator.functional;

import com.google.common.collect.Sets;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorConfigMap;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.StringRule;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 04.04.2023
 */

@Epic("CUSTOM_RULES")
@Feature("CUSTOM_GENERATOR_WITH_CONFIG_MAP")
public class CustomGeneratorWithWithConfigMapTests {

    /*
     * Test Data
     */

    static class Dto {

        @StringRule(words = {"Leila", "Hazel"})
        String witchName;

        @CustomRule(generatorClass = WitchDescriptionGenerator.class)
        String witchDescription;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrew;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrewSecond;

        @NestedDtoRule
        NestedDto nestedDto;

    }

    static class NestedDto {

        @StringRule(words = {"Cruelly", "Monalisa"})
        String witchName;

        @CustomRule(generatorClass = WitchDescriptionGenerator.class)
        String witchDescription;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrew;

        @CustomRule(generatorClass = WitchesBrewGenerator.class)
        WitchesBrew witchesBrewSecond;

    }

    static class WitchesBrew {

        Set<String> ingredients;

        public WitchesBrew(Collection<String> ingredients) {
            this.ingredients = new HashSet<>(ingredients);
        }
    }

    static class WitchesBrewGenerator implements CustomGeneratorConfigMap<WitchesBrew> {

        Map<String, String> configMap;

        @Override
        public WitchesBrew generate() {
            Set<String> ingredients = new HashSet<>();

            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                if (BrewFeature.isItBrewFeature(entry.getKey())) {
                    ingredients.add(entry.getKey() + " " + entry.getValue());
                }
            }

            return new WitchesBrew(ingredients);
        }

        @Override
        public void setConfigMap(Map<String, String> configMap) {
            this.configMap = configMap;
        }
    }

    static class WitchDescriptionGenerator implements CustomGeneratorConfigMap<String> {

        Map<String, String> configMap;


        @Override
        public String generate() {
            String brewFeatures = configMap.entrySet().stream()
                    .filter(r -> BrewFeature.isItBrewFeature(r.getKey()))
                    .map(r -> r.getKey() + " " + String.join(" ", r.getValue()))
                    .collect(Collectors.joining(", "));

            String witchFeature = configMap.entrySet().stream()
                    .filter(r -> WitchFeature.isItWitchFeature(r.getKey()))
                    .map(r -> r.getKey() + " " + String.join(" ", r.getValue()))
                    .collect(Collectors.joining(", "));

            return "The witch is: '" + (witchFeature.isEmpty() ? "UNKNOWN_WITCH" : witchFeature) + "'" +
                    ", she brews: '" + (brewFeatures.isEmpty() ? "UNKNOWN_BREW" : brewFeatures) + "'";
        }

        @Override
        public void setConfigMap(Map<String, String> configMap) {
            this.configMap = configMap;
        }
    }

    enum BrewFeature {
        AGING,
        YOUTH,
        MADNESS,
        BERRY_FLAVOURED;


        public static boolean isItBrewFeature(String value) {
            return Arrays.stream(BrewFeature.values()).map(Enum::name).anyMatch(n -> n.equals(value));
        }
    }

    enum WitchFeature {
        TERRIBLE,
        GRUMPY;

        public static boolean isItWitchFeature(String value) {
            return Arrays.stream(WitchFeature.values()).map(Enum::name).anyMatch(n -> n.equals(value));
        }
    }

    /*
     * Tests
     */


    @Test
    void customGeneratorWithRemarksAsIs() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription,
                        both(containsString("UNKNOWN_BREW")).and(containsString("UNKNOWN_WITCH"))
                ),
                () -> assertThat(dto.witchesBrew.ingredients, empty()),
                () -> assertThat(dto.witchesBrewSecond.ingredients, empty()),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription,
                        both(containsString("UNKNOWN_BREW")).and(containsString("UNKNOWN_WITCH"))
                ),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, empty()),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, empty())
        );
    }

    @Test
    void customGeneratorWithRemarksArgsAddedForAnyField() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .addGeneratorParameter(WitchFeature.TERRIBLE.name(), "as hell")
                .addGeneratorParameter(BrewFeature.BERRY_FLAVOURED.name(), "blueberry")
                .addGeneratorParameter(BrewFeature.YOUTH.name(), "best years")
                .build().generateDto();

        Set<String> ingredients = Sets.newHashSet("YOUTH best years", "BERRY_FLAVOURED blueberry");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("YOUTH best years"),
                        containsString("BERRY_FLAVOURED blueberry"),
                        containsString("TERRIBLE as hell")
                )),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(ingredients)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("YOUTH best years"),
                        containsString("BERRY_FLAVOURED blueberry"),
                        containsString("TERRIBLE as hell")
                )),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredients))
        );
    }


    @Test
    void customGeneratorWithRemarksArgsAddedForGeneratorType() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .addGeneratorParameter(WitchesBrewGenerator.class,
                        "BERRY_FLAVOURED", "strawberry"
                )
                .addGeneratorParameters(WitchDescriptionGenerator.class,
                        "GRUMPY", "always",
                        "MADNESS", "fast")
                .build().generateDto();

        Set<String> ingredients = Sets.newHashSet("BERRY_FLAVOURED strawberry");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription,
                        both(containsString("GRUMPY always")).and(containsString("MADNESS fast"))
                ),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(ingredients)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription,
                        both(containsString("GRUMPY always")).and(containsString("MADNESS fast"))
                ),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredients))
        );
    }

    @Test
    void customGeneratorWithRemarksArgsAddedByField() {

        // remarks set for root dto only

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .addGeneratorParameter("witchDescription", WitchFeature.TERRIBLE.name(), "a little")
                .addGeneratorParameter("witchesBrew", BrewFeature.AGING.name(), "wisdom")
                .addGeneratorParameter("witchesBrewSecond", BrewFeature.MADNESS.name(), "onion");
        Dto dto = builder.build().generateDto();

        Set<String> ingredientsWitchesBrew = Sets.newHashSet("AGING wisdom");
        Set<String> ingredientsWitchesBrewSecond = Sets.newHashSet("MADNESS onion");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("TERRIBLE a little"),
                        containsString("UNKNOWN_BREW")
                )),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredientsWitchesBrew)),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(ingredientsWitchesBrewSecond)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription, allOf(
                        containsString("UNKNOWN_WITCH"),
                        containsString("UNKNOWN_BREW")
                )),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, empty()),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, empty())
        );

        // remarks set for root dto and nested dto

        Set<String> ingredientsWitchesBrewNested = Sets.newHashSet("BERRY_FLAVOURED cherry");
        Set<String> ingredientsWitchesBrewSecondNested = Sets.newHashSet("YOUTH lemon");

        builder
                .addGeneratorParameter("nestedDto.witchDescription", BrewFeature.MADNESS.name(), "garlic")
                .addGeneratorParameter("nestedDto.witchesBrew", BrewFeature.BERRY_FLAVOURED.name(), "cherry")
                .addGeneratorParameter("nestedDto.witchesBrewSecond", BrewFeature.YOUTH.name(), "lemon");

        Dto dto_2 = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto_2.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("TERRIBLE a little"),
                        containsString("UNKNOWN_BREW")
                )),
                () -> assertThat(dto_2.witchesBrew.ingredients, equalTo(ingredientsWitchesBrew)),
                () -> assertThat(dto_2.witchesBrewSecond.ingredients, equalTo(ingredientsWitchesBrewSecond)),

                () -> assertThat(dto_2.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto_2.nestedDto.witchDescription, allOf(
                        containsString("UNKNOWN_WITCH"),
                        containsString("MADNESS garlic")
                )),
                () -> assertThat(dto_2.nestedDto.witchesBrew.ingredients, equalTo(ingredientsWitchesBrewNested)),
                () -> assertThat(dto_2.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredientsWitchesBrewSecondNested))
        );
    }

    @Test
    void customGeneratorWithRemarksOverriddenByTypeAndAnyAndSpecifiedField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .addGeneratorParameter(BrewFeature.BERRY_FLAVOURED.name(), "blueberry")
                .addGeneratorParameter(WitchesBrewGenerator.class, BrewFeature.YOUTH.name(), "sun")
                .addGeneratorParameter("witchesBrew", BrewFeature.MADNESS.name(), "aloe")
                .addGeneratorParameter("witchDescription", WitchFeature.GRUMPY.name(), "as heck")
                .addGeneratorParameter("nestedDto.witchesBrew", BrewFeature.AGING.name(), "watermelon")
                .addGeneratorParameter("nestedDto.witchDescription", WitchFeature.TERRIBLE.name(), "as devil");

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("BERRY_FLAVOURED blueberry"),
                        containsString("GRUMPY as heck"),
                        not(containsString("YOUTH sun"))
                )),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(
                        Sets.newHashSet("BERRY_FLAVOURED blueberry", "YOUTH sun", "MADNESS aloe"))
                ),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(
                        Sets.newHashSet("BERRY_FLAVOURED blueberry", "YOUTH sun"))
                ),
                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription, allOf(
                        containsString("BERRY_FLAVOURED blueberry"),
                        containsString("TERRIBLE as devil"),
                        not(containsString("YOUTH sun"))
                )),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(
                        Sets.newHashSet("BERRY_FLAVOURED blueberry", "YOUTH sun", "AGING watermelon"))
                ),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(
                        Sets.newHashSet("BERRY_FLAVOURED blueberry", "YOUTH sun"))
                )
        );
    }

}
