package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.functional.CustomRemarksTests.FlowerMatcher.checkFlower;
import static org.laoruga.dtogenerator.functional.CustomRemarksTests.FlowerMatcher.checkFlowerAnyPart;

/**
 * @author Il'dar Valitov
 * Created on 31.03.2023
 */
@Epic("CUSTOM_RULES")
@Feature("CUSTOM_REMARKS")
public class CustomRemarksTests {

    interface IDto {
        String getLot();

        Flower getSingleFlower();

        List<Flower> getFlowerBouquet();

        Flower[] getFlowerBouquetArray();

        Map<Flower, Flower> getFlowerBouquetMap();
    }

    @Getter
    static class Dto implements IDto {

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

        @NestedDtoRule
        NestedDto nestedDto;
    }

    @Getter
    static class NestedDto implements IDto {

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

        Dto generatedDto = DtoGenerator.builder(Dto.class)
                .addRuleRemark(FlowerProperty.PETAL_COUNT.setArgs("99"))
                .addRuleRemark(FlowerProperty.REGION.setArgs("Japan"))
                .build()
                .generateDto();

        Matcher<Flower> matcher = checkFlower(99, "Japan");

        Consumer<IDto> assertions = dto ->
                assertAll(
                        () -> assertThat(dto.getSingleFlower(), matcher),

                        () -> assertThat(dto.getFlowerBouquet(), hasSize(10)),
                        () -> assertThat(dto.getFlowerBouquet(), everyItem(matcher)),

                        () -> assertThat(dto.getFlowerBouquetArray().length, equalTo(10)),
                        () -> assertThat(Arrays.asList(dto.getFlowerBouquetArray()), everyItem(matcher)),

                        () -> assertThat(dto.getFlowerBouquetMap().size(), equalTo(1)),
                        () -> assertThat(dto.getFlowerBouquetMap().entrySet().iterator().next().getKey(), matcher),
                        () -> assertThat(dto.getFlowerBouquetMap().entrySet().iterator().next().getKey(), matcher)
                );

        assertions.accept(generatedDto);
        assertions.accept(generatedDto.getNestedDto());

    }

    @Test
    void remarkForSpecificField() {

        final String countries = "Sudan, Tudan, India, Morocco, France, UAR";
        final String countriesNested = "Japan, Kazakhstan, China, Bangladesh";

        Dto dto = DtoGenerator.builder(Dto.class)
                .addRuleRemark("singleFlower",
                        FlowerProperty.PETAL_COUNT.setArgs("1"),
                        FlowerProperty.REGION.setArgs("Moldova"))
                .addRuleRemark("flowerBouquet",
                        FlowerProperty.PETAL_COUNT.setArgs("2"),
                        FlowerProperty.REGION.setArgs("Iran"))
                .addRuleRemark("flowerBouquetArray",
                        FlowerProperty.PETAL_COUNT.setArgs("3"),
                        FlowerProperty.REGION.setArgs(countries))
                .addRuleRemark("flowerBouquetMap",
                        FlowerProperty.PETAL_COUNT.setArgs("4, 5, 6, 7, 8, 9"),
                        FlowerProperty.REGION.setArgs(countries))
                .setTypeGeneratorConfig("flowerBouquetMap", MapConfig.builder().minSize(5).maxSize(5).build())

                // nested
                .addRuleRemark("nestedDto.singleFlower",
                        FlowerProperty.PETAL_COUNT.setArgs("10"),
                        FlowerProperty.REGION.setArgs("Moldova_nested"))
                .addRuleRemark("nestedDto.flowerBouquet",
                        FlowerProperty.PETAL_COUNT.setArgs("20"),
                        FlowerProperty.REGION.setArgs("Iran_nested"))
                .addRuleRemark("nestedDto.flowerBouquetArray",
                        FlowerProperty.PETAL_COUNT.setArgs("30"),
                        FlowerProperty.REGION.setArgs(countriesNested))
                .addRuleRemark("nestedDto.flowerBouquetMap",
                        FlowerProperty.PETAL_COUNT.setArgs("40"),
                        FlowerProperty.REGION.setArgs("Somewhere_nested"))
                .build()
                .generateDto();

        Matcher<Flower> flowerMatcherForMap = checkFlowerAnyPart(new int[]{4, 5, 6, 7, 8, 9}, countries.split(","));

        assertAll(
                () -> assertThat(dto.singleFlower, checkFlower(1, "Moldova")),

                () -> assertThat(dto.flowerBouquet, hasSize(10)),
                () -> assertThat(dto.flowerBouquet, everyItem(checkFlowerAnyPart(2, "Iran"))),

                () -> assertThat(dto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat(Arrays.asList(dto.flowerBouquetArray), everyItem(checkFlowerAnyPart(3, countries.split(",")))),

                () -> assertThat(dto.flowerBouquetMap.size(), equalTo(5)),
                () -> assertThat(dto.flowerBouquetMap.keySet(), everyItem(flowerMatcherForMap)),
                () -> assertThat(dto.flowerBouquetMap.values(), everyItem(flowerMatcherForMap))
        );

        NestedDto nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.singleFlower, checkFlower(10, "Moldova_nested")),

                () -> assertThat(nestedDto.flowerBouquet, hasSize(10)),
                () -> assertThat(nestedDto.flowerBouquet, everyItem(checkFlowerAnyPart(20, "Iran_nested"))),

                () -> assertThat(nestedDto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat(Arrays.asList(nestedDto.flowerBouquetArray), everyItem(checkFlowerAnyPart(30, countriesNested.split(",")))),

                () -> assertThat(nestedDto.flowerBouquetMap.size(), equalTo(1)),
                () -> assertThat(nestedDto.flowerBouquetMap.keySet(), everyItem(checkFlower(40, "Somewhere_nested"))),
                () -> assertThat(nestedDto.flowerBouquetMap.values(), everyItem(checkFlower(40, "Somewhere_nested")))
        );

    }

    @Test
    void remarkForAllANdSpecificFields() {

        final String countries = "Sudan, Tudan, India, Morocco, France, UAR";
        final String allFieldCounty = "Japan";
        int[] allFieldPetals = new int[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

        Dto dto = DtoGenerator.builder(Dto.class)

                // any fields
                .addRuleRemark(FlowerProperty.PETAL_COUNT.setArgs("11, 12, 13, 14, 15, 16, 17, 18, 19, 20"))
                .addRuleRemark(FlowerProperty.REGION.setArgs(allFieldCounty))

                // specific fields
                .addRuleRemark("singleFlower",
                        FlowerProperty.REGION.setArgs("Moldova"))
                .addRuleRemark("flowerBouquet",
                        FlowerProperty.PETAL_COUNT.setArgs("2"))
                .addRuleRemark("flowerBouquetMap",
                        FlowerProperty.REGION.setArgs(countries))
                .setTypeGeneratorConfig("flowerBouquetMap", MapConfig.builder().minSize(5).maxSize(5).build())

                .addRuleRemark("nestedDto.singleFlower",
                        FlowerProperty.PETAL_COUNT.setArgs("10"))
                .addRuleRemark("nestedDto.flowerBouquetArray",
                        FlowerProperty.REGION.setArgs("Iran_nested"))
                .addRuleRemark("nestedDto.flowerBouquetMap",
                        FlowerProperty.REGION.setArgs("Somewhere_nested"))
                .build()
                .generateDto();


        assertAll(
                () -> assertThat("Any + field remark", dto.singleFlower, checkFlowerAnyPart(allFieldPetals, "Moldova")),

                () -> assertThat("Any + field remark", dto.flowerBouquet, hasSize(10)),
                () -> assertThat("Any + field remark", dto.flowerBouquet, everyItem(checkFlowerAnyPart(2, allFieldCounty))),

                () -> assertThat("Any-field remark only", dto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat("Any-field remark only", Arrays.asList(dto.flowerBouquetArray), everyItem(checkFlowerAnyPart(allFieldPetals, allFieldCounty))),

                () -> assertThat("Any + field remark", dto.flowerBouquetMap.size(), equalTo(5)),
                () -> assertThat("Any + field remark", dto.flowerBouquetMap.keySet(), everyItem(checkFlowerAnyPart(allFieldPetals, countries.split(",")))),
                () -> assertThat("Any + field remark", dto.flowerBouquetMap.values(), everyItem(checkFlowerAnyPart(allFieldPetals, countries.split(","))))
        );

        NestedDto nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat("Any + field remark", nestedDto.singleFlower, checkFlower(10, allFieldCounty)),

                () -> assertThat("Any-field remark only", nestedDto.flowerBouquet, hasSize(10)),
                () -> assertThat("Any-field remark only", nestedDto.flowerBouquet, everyItem(checkFlowerAnyPart(allFieldPetals, allFieldCounty))),

                () -> assertThat("Any + field remark", nestedDto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat("Any + field remark", Arrays.asList(nestedDto.flowerBouquetArray), everyItem(checkFlowerAnyPart(allFieldPetals, "Iran_nested"))),

                () -> assertThat("Any + field remark", nestedDto.flowerBouquetMap.size(), equalTo(1)),
                () -> assertThat("Any + field remark", nestedDto.flowerBouquetMap.keySet(), everyItem(checkFlowerAnyPart(allFieldPetals, "Somewhere_nested"))),
                () -> assertThat("Any + field remark", nestedDto.flowerBouquetMap.values(), everyItem(checkFlowerAnyPart(allFieldPetals, "Somewhere_nested")))
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
