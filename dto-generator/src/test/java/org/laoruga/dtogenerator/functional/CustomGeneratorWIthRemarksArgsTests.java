package org.laoruga.dtogenerator.functional;

import com.google.common.collect.Sets;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorRemarkableArgs;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Il'dar Valitov
 * Created on 04.04.2023
 */

@Epic("CUSTOM_RULES")
@Feature("CUSTOM_GENERATOR_WITH_REMARKS_ARGS")
public class CustomGeneratorWIthRemarksArgsTests {

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

    static class WitchesBrewGenerator implements CustomGeneratorRemarkableArgs<WitchesBrew> {

        Map<CustomRuleRemark, CustomRuleRemarkArgs> ruleRemarks;

        @Override
        public WitchesBrew generate() {
            Set<String> ingredients = new HashSet<>();

            for (Map.Entry<CustomRuleRemark, CustomRuleRemarkArgs> entry : ruleRemarks.entrySet()) {
                if (entry.getKey().getClass() == BrewFeature.class) {
                    ingredients.add(entry.getKey() + " " + entry.getValue().getArgs()[0]);
                }
            }

            return new WitchesBrew(ingredients);
        }

        @Override
        public void setRuleRemarks(Map<CustomRuleRemark, CustomRuleRemarkArgs> ruleRemarks) {
            this.ruleRemarks = ruleRemarks;
        }
    }

    static class WitchDescriptionGenerator implements CustomGeneratorRemarkableArgs<String> {

        Map<CustomRuleRemark, CustomRuleRemarkArgs> ruleRemarks;

        @Override
        public String generate() {
            String brewFeatures = ruleRemarks.entrySet().stream()
                    .filter(r -> r.getKey().getClass() == BrewFeature.class)
                    .map(r -> r.getKey() + " " + String.join(" ", r.getValue().getArgs()))
                    .collect(Collectors.joining(", "));

            String witchFeature = ruleRemarks.entrySet().stream()
                    .filter(r -> r.getKey().getClass() == WitchFeature.class)
                    .map(r -> r.getKey() + " " + String.join(" ", r.getValue().getArgs()))
                    .collect(Collectors.joining(", "));

            return "The witch is: '" + (witchFeature.isEmpty() ? "UNKNOWN_WITCH" : witchFeature) + "'" +
                    ", she brews: '" + (brewFeatures.isEmpty() ? "UNKNOWN_BREW" : brewFeatures) + "'";
        }

        @Override
        public void setRuleRemarks(Map<CustomRuleRemark, CustomRuleRemarkArgs> ruleRemarks) {
            this.ruleRemarks = ruleRemarks;
        }
    }

    enum BrewFeature implements CustomRuleRemarkArgs {
        AGING,
        YOUTH,
        MADNESS,
        BERRY_FLAVOURED;

        @Override
        public int minimumArgsNumber() {
            return 1;
        }
    }

    enum WitchFeature implements CustomRuleRemarkArgs {
        TERRIBLE,
        GRUMPY;

        @Override
        public int minimumArgsNumber() {
            return 1;
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
                .addRuleRemark(WitchFeature.TERRIBLE.setArgs("as hell"))
                .addRuleRemark(BrewFeature.BERRY_FLAVOURED.setArgs("blueberry"))
                .addRuleRemark(BrewFeature.YOUTH.setArgs("best years"))
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
                .addRuleRemark(WitchesBrewGenerator.class,
                        BrewFeature.BERRY_FLAVOURED.setArgs("strawberry")
                )
                .addRuleRemark(WitchDescriptionGenerator.class,
                        WitchFeature.GRUMPY.setArgs("always"),
                        BrewFeature.MADNESS.setArgs("fast", "slow")
                )
                .build().generateDto();

        Set<String> ingredients = Sets.newHashSet("BERRY_FLAVOURED strawberry");

        assertAll(
                () -> assertThat(dto.witchName, notNullValue()),
                () -> assertThat(dto.witchDescription,
                        both(containsString("GRUMPY always")).and(containsString("MADNESS fast slow"))
                ),
                () -> assertThat(dto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.witchesBrewSecond.ingredients, equalTo(ingredients)),

                () -> assertThat(dto.nestedDto.witchName, notNullValue()),
                () -> assertThat(dto.nestedDto.witchDescription,
                        both(containsString("GRUMPY always")).and(containsString("MADNESS fast slow"))
                ),
                () -> assertThat(dto.nestedDto.witchesBrew.ingredients, equalTo(ingredients)),
                () -> assertThat(dto.nestedDto.witchesBrewSecond.ingredients, equalTo(ingredients))
        );
    }

    @Test
    void customGeneratorWithRemarksArgsAddedByField() {

        // remarks set for root dto only

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .addRuleRemark("witchDescription", WitchFeature.TERRIBLE.setArgs("a little"))
                .addRuleRemark("witchesBrew", BrewFeature.AGING.setArgs("wisdom"))
                .addRuleRemark("witchesBrewSecond", BrewFeature.MADNESS.setArgs("onion"));
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
                .addRuleRemark("nestedDto.witchDescription", BrewFeature.MADNESS.setArgs("garlic"))
                .addRuleRemark("nestedDto.witchesBrew", BrewFeature.BERRY_FLAVOURED.setArgs("cherry"))
                .addRuleRemark("nestedDto.witchesBrewSecond", BrewFeature.YOUTH.setArgs("lemon"));

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
                .addRuleRemark(BrewFeature.BERRY_FLAVOURED.setArgs("blueberry"))
                .addRuleRemark(WitchesBrewGenerator.class, BrewFeature.YOUTH.setArgs("sun"))
                .addRuleRemark("witchesBrew", BrewFeature.MADNESS.setArgs("aloe"))
                .addRuleRemark("witchDescription", WitchFeature.GRUMPY.setArgs("as heck"))
                .addRuleRemark("nestedDto.witchesBrew", BrewFeature.AGING.setArgs("watermelon"))
                .addRuleRemark("nestedDto.witchDescription", WitchFeature.TERRIBLE.setArgs("as devil"));

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

    @Test
    @Feature("NEGATIVE_TEST")
    void violationOfNumberOfRemarkArgs() {

        assertThrows(
                DtoGeneratorException.class,
                () -> DtoGenerator.builder(Dto.class).addRuleRemark(BrewFeature.BERRY_FLAVOURED),
                "Remark 'BERRY_FLAVOURED' expected at least '1' arg(s). Passed '0' arg(s)."
        );

    }

}
