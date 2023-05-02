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
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorConfigMap;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.CharSet;
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
import static org.laoruga.dtogenerator.functional.CustomGeneratorsWithRemarksDtoDependentTests.FlowerMatcher.flowerMatcher;
import static org.laoruga.dtogenerator.functional.CustomGeneratorsWithRemarksDtoDependentTests.FlowerMatcher.flowerMatcherAnyPart;

/**
 * @author Il'dar Valitov
 * Created on 31.03.2023
 */
@Epic("CUSTOM_RULES")
@Feature("CUSTOM_REMARKS")
public class CustomGeneratorsWithRemarksDtoDependentTests {

    interface IDto {
        String getLot();

        Flower getSingleFlower();

        List<Flower> getFlowerBouquet();

        Flower[] getFlowerBouquetArray();

        Map<Flower, Flower> getFlowerBouquetMap();
    }

    @Getter
    static class Dto implements IDto {

        @StringRule(minLength = 5, maxLength = 5, chars = CharSet.ENG + CharSet.NUM)
        String lot;

        @CustomRule(generatorClass = FlowerGenerator.class)
        Flower singleFlower;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = FlowerGenerator.class)))
        List<Flower> flowerBouquet;

        @ArrayRule(minLength = 10, element =
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

        @StringRule(minLength = 1, maxLength = 1)
        String lot;

        @CustomRule(generatorClass = FlowerGenerator.class)
        Flower singleFlower;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = FlowerGenerator.class)))
        List<Flower> flowerBouquet;

        @ArrayRule(minLength = 10, element =
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

    enum FlowerProperty {
        REGION,
        PETAL_COUNT;

    }

    @Test
    void remarkForAllFields() {

        Dto generatedDto = DtoGenerator.builder(Dto.class)
                .addGeneratorParameter(FlowerProperty.PETAL_COUNT.name(), "99")
                .addGeneratorParameter(FlowerProperty.REGION.name(), "Japan")
                .build()
                .generateDto();

        Matcher<Flower> matcher = flowerMatcher(99, generatedDto.lot, "Japan");

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
                .addGeneratorParameters("singleFlower",
                        FlowerProperty.PETAL_COUNT.name(), "1",
                        FlowerProperty.REGION.name(), "Moldova")
                .addGeneratorParameters("flowerBouquet",
                        FlowerProperty.PETAL_COUNT.name(), "2",
                        FlowerProperty.REGION.name(), "Iran")
                .addGeneratorParameters("flowerBouquetArray",
                        FlowerProperty.PETAL_COUNT.name(), "3",
                        FlowerProperty.REGION.name(), countries)
                .addGeneratorParameters("flowerBouquetMap",
                        FlowerProperty.PETAL_COUNT.name(), "4, 5, 6, 7, 8, 9",
                        FlowerProperty.REGION.name(), countries)
                .setGeneratorConfig("flowerBouquetMap", MapConfig.builder().minSize(5).maxSize(5).build())

                // nested
                .addGeneratorParameters("nestedDto.singleFlower",
                        FlowerProperty.PETAL_COUNT.name(), "10",
                        FlowerProperty.REGION.name(), "Moldova_nested")
                .addGeneratorParameters("nestedDto.flowerBouquet",
                        FlowerProperty.PETAL_COUNT.name(), "20",
                        FlowerProperty.REGION.name(), "Iran_nested")
                .addGeneratorParameters("nestedDto.flowerBouquetArray",
                        FlowerProperty.PETAL_COUNT.name(), "30",
                        FlowerProperty.REGION.name(), countriesNested)
                .addGeneratorParameters("nestedDto.flowerBouquetMap",
                        FlowerProperty.PETAL_COUNT.name(), "40",
                        FlowerProperty.REGION.name(), "Somewhere_nested")
                .build()
                .generateDto();

        Matcher<Flower> flowerMatcherForMap = FlowerMatcher
                .flowerMatcherAnyPart(new int[]{4, 5, 6, 7, 8, 9}, countries.split(","));

        assertAll(
                () -> assertThat(dto.singleFlower, flowerMatcher(1, "Moldova")),

                () -> assertThat(dto.flowerBouquet, hasSize(10)),
                () -> assertThat(dto.flowerBouquet, everyItem(flowerMatcherAnyPart(2, "Iran"))),

                () -> assertThat(dto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat(Arrays.asList(dto.flowerBouquetArray),
                        everyItem(flowerMatcherAnyPart(3, countries.split(",")))),

                () -> assertThat(dto.flowerBouquetMap.size(), equalTo(5)),
                () -> assertThat(dto.flowerBouquetMap.keySet(), everyItem(flowerMatcherForMap)),
                () -> assertThat(dto.flowerBouquetMap.values(), everyItem(flowerMatcherForMap))
        );

        NestedDto nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.singleFlower, flowerMatcher(10, "Moldova_nested")),

                () -> assertThat(nestedDto.flowerBouquet, hasSize(10)),
                () -> assertThat(nestedDto.flowerBouquet,
                        everyItem(flowerMatcherAnyPart(20, "Iran_nested"))),

                () -> assertThat(nestedDto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat(Arrays.asList(nestedDto.flowerBouquetArray),
                        everyItem(flowerMatcherAnyPart(30, countriesNested.split(",")))),

                () -> assertThat(nestedDto.flowerBouquetMap.size(), equalTo(1)),
                () -> assertThat(nestedDto.flowerBouquetMap.keySet(),
                        everyItem(flowerMatcher(40, "Somewhere_nested"))),
                () -> assertThat(nestedDto.flowerBouquetMap.values(),
                        everyItem(flowerMatcher(40, "Somewhere_nested")))
        );

    }

    @Test
    void remarkForAllANdSpecificFields() {

        final String countries = "Sudan, Tudan, India, Morocco, France, UAR";
        final String allFieldCounty = "Japan";
        int[] allFieldPetals = new int[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

        Dto dto = DtoGenerator.builder(Dto.class)

                // any fields
                .addGeneratorParameter(FlowerProperty.PETAL_COUNT.name(), "11, 12, 13, 14, 15, 16, 17, 18, 19, 20")
                .addGeneratorParameter(FlowerProperty.REGION.name(), allFieldCounty)

                // specific fields
                .addGeneratorParameter("singleFlower",
                        FlowerProperty.REGION.name(), "Moldova")
                .addGeneratorParameter("flowerBouquet",
                        FlowerProperty.PETAL_COUNT.name(), "2")
                .addGeneratorParameter("flowerBouquetMap",
                        FlowerProperty.REGION.name(), countries)
                .setGeneratorConfig("flowerBouquetMap", MapConfig.builder().minSize(5).maxSize(5).build())

                .addGeneratorParameter("nestedDto.singleFlower",
                        FlowerProperty.PETAL_COUNT.name(), "10")
                .addGeneratorParameter("nestedDto.flowerBouquetArray",
                        FlowerProperty.REGION.name(), "Iran_nested")
                .addGeneratorParameter("nestedDto.flowerBouquetMap",
                        FlowerProperty.REGION.name(), "Somewhere_nested")
                .build()
                .generateDto();


        assertAll(
                () -> assertThat("Any + field remark", dto.singleFlower, FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, "Moldova")),

                () -> assertThat("Any + field remark", dto.flowerBouquet, hasSize(10)),
                () -> assertThat("Any + field remark", dto.flowerBouquet,
                        everyItem(flowerMatcherAnyPart(2, allFieldCounty))),

                () -> assertThat("Any-field remark only", dto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat("Any-field remark only", Arrays.asList(dto.flowerBouquetArray),
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, allFieldCounty))),

                () -> assertThat("Any + field remark", dto.flowerBouquetMap.size(), equalTo(5)),
                () -> assertThat("Any + field remark", dto.flowerBouquetMap.keySet(),
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, countries.split(",")))),
                () -> assertThat("Any + field remark", dto.flowerBouquetMap.values(),
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, countries.split(","))))
        );

        NestedDto nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat("Any + field remark", nestedDto.singleFlower,
                        flowerMatcher(10, allFieldCounty)),

                () -> assertThat("Any-field remark only", nestedDto.flowerBouquet, hasSize(10)),
                () -> assertThat("Any-field remark only", nestedDto.flowerBouquet,
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, allFieldCounty))),

                () -> assertThat("Any + field remark", nestedDto.flowerBouquetArray.length, equalTo(10)),
                () -> assertThat("Any + field remark", Arrays.asList(nestedDto.flowerBouquetArray),
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, "Iran_nested"))),

                () -> assertThat("Any + field remark", nestedDto.flowerBouquetMap.size(), equalTo(1)),
                () -> assertThat("Any + field remark", nestedDto.flowerBouquetMap.keySet(),
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, "Somewhere_nested"))),
                () -> assertThat("Any + field remark", nestedDto.flowerBouquetMap.values(),
                        everyItem(FlowerMatcher.flowerMatcherAnyPart(allFieldPetals, "Somewhere_nested")))
        );
    }

    /*
     * Custom generator
     */

    private static final String[] COUNTRIES = {"Italy", "USA", "Ukraine"};

    static class FlowerGenerator implements
            CustomGeneratorArgs<Flower>,
            CustomGeneratorConfigMap<Flower>,
            CustomGeneratorDtoDependent<Flower, Dto> {
        Map<String, String> configMap;
        Supplier<Dto> generatedDto;

        String[] args;

        @Override
        public Flower generate() {

            int petalNumber;

            if (configMap.containsKey(FlowerProperty.PETAL_COUNT.name())) {
                String[] numbers = configMap.get(FlowerProperty.PETAL_COUNT.name()).split(",");
                petalNumber = Integer.parseInt(RandomUtils.getRandomItem(numbers).trim());
            } else {
                petalNumber = RandomUtils.nextInt(10, 100);
            }

            String region;

            if (configMap.containsKey(FlowerProperty.REGION.name())) {
                String[] regions = configMap.get(FlowerProperty.REGION.name()).split(",");
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
        public void setDtoSupplier(Supplier<Dto> generatedDto) {
            this.generatedDto = generatedDto;
        }

        @Override
        public boolean isDtoReady() {
            return generatedDto.get().lot != null;
        }

        @Override
        public void setArgs(String... args) {
            this.args = args;
        }

        @Override
        public void setConfigMap(Map<String, String> configMap) {
            this.configMap = configMap;
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

        public static Matcher<Flower> flowerMatcher(int petalCount, String... nameParts) {
            return new FlowerMatcher(new int[]{petalCount}, nameParts, true);
        }

        public static Matcher<Flower> flowerMatcherAnyPart(int petalCount, String... anyPart) {
            return new FlowerMatcher(new int[]{petalCount}, anyPart, false);
        }

        public static Matcher<Flower> flowerMatcherAnyPart(int[] petalCount, String... anyPart) {
            return new FlowerMatcher(petalCount, anyPart, false);
        }
    }

}
