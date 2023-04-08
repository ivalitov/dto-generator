package org.laoruga.dtogenerator.functional;

import com.google.common.collect.Sets;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemark;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.StringRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 04.04.2023
 */

@Epic("CUSTOM_RULES")
@Feature("CUSTOM_GENERATOR_WITH_REMARKS")
public class CustomGeneratorWIthRemarksTests {

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

    static class WitchesBrewGenerator implements CustomGeneratorRemarkable<WitchesBrew> {

        Set<CustomRuleRemark> ruleRemarks;

        @Override
        public WitchesBrew generate() {
            Set<String> ingredients = new HashSet<>();
            for (CustomRuleRemark ruleRemark : ruleRemarks) {
                if (ruleRemark.getClass() == BrewFeature.class) {

                    switch ((BrewFeature) ruleRemark) {
                        case AGING:
                            ingredients.add("leaves");
                            break;
                        case YOUTH:
                            ingredients.add("breath");
                            break;
                        case MADNESS:
                            ingredients.add("cactus");
                            break;
                        case BERRY_FLAVOURED:
                            ingredients.add("blueberry");
                            break;
                    }
                }
            }
            return new WitchesBrew(ingredients);
        }

        @Override
        public void setRuleRemarks(Set<CustomRuleRemark> ruleRemarks) {
            this.ruleRemarks = ruleRemarks;
        }
    }

    static class WitchDescriptionGenerator implements CustomGeneratorRemarkable<String> {

        Set<CustomRuleRemark> ruleRemarks;

        @Override
        public String generate() {
            String brewFeatures = ruleRemarks.stream()
                    .filter(r -> r.getClass() == BrewFeature.class)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            String witchFeature = ruleRemarks.stream()
                    .filter(r -> r.getClass() == WitchFeature.class)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return "The witch is: '" + (witchFeature.isEmpty() ? "UNKNOWN_WITCH" : witchFeature) + "'" +
                    ", she brews: '" + (brewFeatures.isEmpty() ? "UNKNOWN_BREW" : brewFeatures) + "'";
        }

        @Override
        public void setRuleRemarks(Set<CustomRuleRemark> ruleRemarks) {
            this.ruleRemarks = ruleRemarks;
        }
    }

    enum BrewFeature implements CustomRuleRemark {
        AGING,
        YOUTH,
        MADNESS,
        BERRY_FLAVOURED
    }

    enum WitchFeature implements CustomRuleRemark {
        TERRIBLE,
        GRUMPY
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
    void customGeneratorWithRemarksAddedForAnyField() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .addRuleRemark(BrewFeature.BERRY_FLAVOURED)
                .addRuleRemark(BrewFeature.YOUTH)
                .build().generateDto();

        Set<String> ingredients = Sets.newHashSet("blueberry", "breath");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("YOUTH"),
                        containsString("BERRY_FLAVOURED"),
                        containsString("UNKNOWN_WITCH")
                )),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(ingredients)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription, allOf(
                        containsString("YOUTH"),
                        containsString("BERRY_FLAVOURED"),
                        containsString("UNKNOWN_WITCH")
                )),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredients))
        );
    }

    @Test
    void customGeneratorWithRemarksAddedForGeneratorType() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .addRuleRemark(WitchesBrewGenerator.class, BrewFeature.BERRY_FLAVOURED)
                .addRuleRemark(WitchDescriptionGenerator.class, WitchFeature.GRUMPY, BrewFeature.MADNESS)
                .build().generateDto();

        Set<String> ingredients = Sets.newHashSet("blueberry");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription,
                        both(containsString("GRUMPY")).and(containsString("MADNESS"))
                ),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(ingredients)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription,
                        both(containsString("GRUMPY")).and(containsString("MADNESS"))
                ),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredients))
        );
    }

    @Test
    void customGeneratorWithRemarksAddedByField() {

        // remarks set for root dto only

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .addRuleRemark("witchDescription", WitchFeature.TERRIBLE)
                .addRuleRemark("witchesBrew", BrewFeature.AGING)
                .addRuleRemark("witchesBrewSecond", BrewFeature.MADNESS);
        Dto dto = builder.build().generateDto();

        Set<String> ingredientsWitchesBrew = Sets.newHashSet("leaves");
        Set<String> ingredientsWitchesBrewSecond = Sets.newHashSet("cactus");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("TERRIBLE"),
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

        Set<String> ingredientsWitchesBrewNested = Sets.newHashSet("blueberry");
        Set<String> ingredientsWitchesBrewSecondNested = Sets.newHashSet("breath");

        builder
                .addRuleRemark("nestedDto.witchDescription", BrewFeature.MADNESS)
                .addRuleRemark("nestedDto.witchesBrew", BrewFeature.BERRY_FLAVOURED)
                .addRuleRemark("nestedDto.witchesBrewSecond", BrewFeature.YOUTH);

        Dto dto_2 = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto_2.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("TERRIBLE"),
                        containsString("UNKNOWN_BREW")
                )),
                () -> assertThat(dto_2.witchesBrew.ingredients, equalTo(ingredientsWitchesBrew)),
                () -> assertThat(dto_2.witchesBrewSecond.ingredients, equalTo(ingredientsWitchesBrewSecond)),

                () -> assertThat(dto_2.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto_2.nestedDto.witchDescription, allOf(
                        containsString("UNKNOWN_WITCH"),
                        containsString("MADNESS")
                )),
                () -> assertThat(dto_2.nestedDto.witchesBrew.ingredients, equalTo(ingredientsWitchesBrewNested)),
                () -> assertThat(dto_2.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredientsWitchesBrewSecondNested))
        );
    }

    @Test
    void customGeneratorWithRemarksOverriddenByTypeAndAnyAndSpecifiedField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .addRuleRemark(BrewFeature.BERRY_FLAVOURED)
                .addRuleRemark(WitchesBrewGenerator.class, BrewFeature.YOUTH)
                .addRuleRemark("witchesBrew", BrewFeature.MADNESS)
                .addRuleRemark("witchDescription", WitchFeature.GRUMPY)
                .addRuleRemark("nestedDto.witchesBrew", BrewFeature.AGING)
                .addRuleRemark("nestedDto.witchDescription", WitchFeature.TERRIBLE);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription, allOf(
                        containsString("BERRY_FLAVOURED"),
                        containsString("GRUMPY"),
                        not(containsString("YOUTH"))
                )),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(Sets.newHashSet("blueberry", "breath", "cactus"))),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(Sets.newHashSet("blueberry", "breath"))),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription, allOf(
                        containsString("BERRY_FLAVOURED"),
                        containsString("TERRIBLE"),
                        not(containsString("YOUTH"))
                )),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(Sets.newHashSet("blueberry", "breath", "leaves"))),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(Sets.newHashSet("blueberry", "breath")))
        );

    }

}
