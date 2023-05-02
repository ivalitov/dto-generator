package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.CharSet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 31.03.2023
 */
@Epic("CUSTOM_RULES")
@Feature("DTO_DEPENDENT_GENERATOR")
public class CustomGeneratorDtoDependentTests {

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

        @StringRule(minLength = 5, maxLength = 5, chars = CharSet.NUM)
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

    @Getter
    static class NestedDtoDeeper implements IDto {

        @StringRule(minLength = 5, maxLength = 5, chars = CharSet.NUM)
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
        String lot;
    }


    @Test
    void dtoDependentGeneratorTest() {

        Dto generatedDto = DtoGenerator.builder(Dto.class)
                .build()
                .generateDto();

        FlowerMatcher matcher = new FlowerMatcher(generatedDto.lot);

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

    /*
     * Custom generator
     */

    private static final String[] COUNTRIES = {"Italy", "USA", "Ukraine"};

    static class FlowerGenerator implements CustomGeneratorDtoDependent<Flower, Dto> {

        Supplier<Dto> generatedDto;


        @Override
        public Flower generate() {

            return new Flower(
                    "Lot: " + generatedDto.get().lot
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
    }


    /*
     * Hamcrest matcher
     */

    static class FlowerMatcher extends TypeSafeMatcher<Flower> {

        private final String lot;

        public FlowerMatcher(String lot) {
            this.lot = lot;
        }

        @Override
        protected boolean matchesSafely(Flower item) {
            if (item == null || item.lot == null) {
                return false;
            }
            return item.lot.contains(lot);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Expected lot not found: " + lot + "'");
        }
    }

}
