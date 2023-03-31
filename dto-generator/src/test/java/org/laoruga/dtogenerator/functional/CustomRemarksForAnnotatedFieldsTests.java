package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkableArgs;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemarkArgs;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.generator.config.dto.MapConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.functional.CustomRemarksForAnnotatedFieldsTests.FlowerMatcher.checkFlower;
import static org.laoruga.dtogenerator.functional.CustomRemarksForAnnotatedFieldsTests.FlowerMatcher.checkFlowerAnyPart;

/**
 * @author Il'dar Valitov
 * Created on 31.03.2023
 */
@Epic("CUSTOM_RULES")
@Feature("CUSTOM_REMARKS")
public class CustomRemarksForAnnotatedFieldsTests {

    static class Dto {

        @StringRule(minLength = 1, maxLength = 5)
        String lot;

        @CustomRule(generatorClass = FlowerGenerator.class)
        Flower singleFlower;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = FlowerGenerator.class)))
        List<Flower> flowerBouquet;

        @ArrayRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = FlowerGenerator.class)))
        Flower[] flowerBouquetArray;

        @MapRule(maxSize = 1,
                key = @Entry(customRule = @CustomRule(generatorClass = FlowerGenerator.class)),
                value = @Entry(customRule = @CustomRule(generatorClass = FlowerGenerator.class))
        )
        Map<Flower, Flower> flowerBouquetMap;

    }

    @AllArgsConstructor
    @EqualsAndHashCode
    static class Flower {
        String name;
        Integer petalCount;
    }

    enum FlowerProperty implements ICustomRuleRemarkArgs {
        REGION,
        PETAL_COUNT;

        @Override
        public int requiredArgsNumber() {
            return 1;
        }
    }


    @Test
    void remarkForAllFields() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .addRuleRemark(FlowerProperty.PETAL_COUNT.setArgs("99"))
                .addRuleRemark(FlowerProperty.REGION.setArgs("Japan"))
                .build()
                .generateDto();

        Matcher<Flower> matcher = checkFlower(99, "Japan");

        assertAll(
                () -> assertThat(dto.singleFlower, matcher),

                () -> assertThat(dto.flowerBouquet, hasSize(10)),
                () -> assertThat(dto.flowerBouquet, everyItem(matcher)),

                () -> assertThat(dto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat(Arrays.asList(dto.flowerBouquetArray), everyItem(matcher)),

                () -> assertThat(dto.flowerBouquetMap.size(), equalTo(1)),
                () -> assertThat(dto.flowerBouquetMap.entrySet().iterator().next().getKey(), matcher),
                () -> assertThat(dto.flowerBouquetMap.entrySet().iterator().next().getKey(), matcher)
        );

    }

    @Test
    void remarkForSpecificField() {

        final String countries = "Sudan, Tudan, India, Morocco, France, UAR";

        Dto dto = DtoGenerator.builder(Dto.class)
                .addRuleRemark("singleFlower",
                        FlowerProperty.PETAL_COUNT.setArgs("1"),
                        FlowerProperty.REGION.setArgs("Moldova"))
                .addRuleRemark("flowerBouquet",
                        FlowerProperty.PETAL_COUNT.setArgs("2"))
                .addRuleRemark("flowerBouquetArray",
                        FlowerProperty.PETAL_COUNT.setArgs("3"),
                        FlowerProperty.REGION.setArgs(countries))
                .addRuleRemark("flowerBouquetMap",
                        FlowerProperty.PETAL_COUNT.setArgs("4, 5, 6, 7, 8, 9"),
                        FlowerProperty.REGION.setArgs(countries))
                .setTypeGeneratorConfig("flowerBouquetMap", MapConfig.builder().minSize(5).maxSize(5).build())
                .build()
                .generateDto();

        Matcher<Flower> flowerMatcherForMap = checkFlowerAnyPart(new int[]{4, 5, 6, 7, 8, 9}, countries.split(","));

        assertAll(
                () -> assertThat(dto.singleFlower, checkFlower(1, "Moldova")),

                () -> assertThat(dto.flowerBouquet, hasSize(10)),
                () -> assertThat(dto.flowerBouquet, everyItem(checkFlowerAnyPart(2, COUNTRIES))),

                () -> assertThat(dto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat(Arrays.asList(dto.flowerBouquetArray), everyItem(checkFlowerAnyPart(3, countries.split(",")))),

                () -> assertThat(dto.flowerBouquetMap.size(), equalTo(5)),
                () -> assertThat(dto.flowerBouquetMap.keySet(), everyItem(flowerMatcherForMap)),
                () -> assertThat(dto.flowerBouquetMap.values(), everyItem(flowerMatcherForMap))
        );

    }

    /*
     * Custom generator
     */

    private static final String[] COUNTRIES = {"Italy", "USA", "Ukraine"};

    static class FlowerGenerator implements
            ICustomGeneratorRemarkableArgs<Flower>,
            ICustomGeneratorDtoDependent<Flower, Dto> {

        Map<ICustomRuleRemark, ICustomRuleRemarkArgs> ruleRemarks;
        Supplier<Dto> generatedDto;

        @Override
        public Flower generate() {

            int petalNumber;

            if (ruleRemarks.containsKey(FlowerProperty.PETAL_COUNT)) {
                String[] numbers = ruleRemarks.get(FlowerProperty.PETAL_COUNT).getArgs()[0].split(",");
                petalNumber = Integer.parseInt(RandomUtils.getRandomItem(numbers).trim());
            } else {
                petalNumber = RandomUtils.nextInt(10, 100);
            }

            String region;

            if (ruleRemarks.containsKey(FlowerProperty.REGION)) {
                String[] regions = ruleRemarks.get(FlowerProperty.REGION).getArgs()[0].split(",");
                region = RandomUtils.getRandomItem(regions).trim();
            } else {
                region = RandomUtils.getRandomItem(COUNTRIES);
            }

            return new Flower(
                    "Lot: " + generatedDto.get().lot + " Region: " + region,
                    petalNumber
            );
        }

        @Override
        public void setRuleRemarks(Map<ICustomRuleRemark, ICustomRuleRemarkArgs> ruleRemarks) {
            this.ruleRemarks = ruleRemarks;
        }

        @Override
        public void setDtoSupplier(Supplier<Dto> generatedDto) {
            this.generatedDto = generatedDto;
        }

        @Override
        public boolean isDtoReady() {
            return generatedDto.get().lot != null;
        }
    }


    /*
     * Hamcrest matcher
     */

    static class FlowerMatcher extends TypeSafeMatcher<Flower> {

        private final int[] petalCount;
        private final String[] nameParts;
        private final boolean allParts;

        public FlowerMatcher(int[] petalCount, String[] nameParts, boolean allParts) {
            this.petalCount = petalCount;
            this.nameParts = nameParts;
            this.allParts = allParts;
        }

        @Override
        protected boolean matchesSafely(Flower item) {
            if (item == null || Arrays.stream(petalCount).noneMatch(petals -> petals == item.petalCount)) {
                return false;
            }
            if (allParts) {
                return Arrays.stream(nameParts).allMatch(part -> item.name.contains(part));
            }
            return Arrays.stream(nameParts).anyMatch(part -> item.name.contains(part));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Wrong petal count or name parts.");
        }

        public static Matcher<Flower> checkFlower(int petalCount, String... nameParts) {
            return new FlowerMatcher(new int[]{petalCount}, nameParts, true);
        }

        public static Matcher<Flower> checkFlowerAnyPart(int petalCount, String... anyPart) {
            return new FlowerMatcher(new int[]{petalCount}, anyPart, false);
        }

        public static Matcher<Flower> checkFlowerAnyPart(int[] petalCount, String... anyPart) {
            return new FlowerMatcher(petalCount, anyPart, false);
        }
    }

}
