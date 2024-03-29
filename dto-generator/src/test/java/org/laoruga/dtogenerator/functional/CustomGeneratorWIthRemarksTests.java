package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorBoundary;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.constants.Boundary.*;
import static org.laoruga.dtogenerator.functional.CustomGeneratorWIthRemarksTests.OctopusMatcher.*;

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

    static final String KIND_MIN = "Oct";
    static final String KIND_MAX = "Octopus";

    static class Dto {

        @CustomRule(generatorClass = OctopusKindGenerator.class)
        String octopusKind;

        @CustomRule(generatorClass = OctopusKindGenerator.class, boundary = NULL_VALUE)
        String octopusKindSecond;

        @CustomRule(generatorClass = OctopusGenerator.class)
        Octopus octopus;

        @CustomRule(generatorClass = OctopusGenerator.class, boundary = MAX_VALUE)
        Octopus octopusSecond;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class, boundary = MAX_VALUE)))
        List<Octopus> octopusList;

        @ArrayRule(minLength = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class)))
        Octopus[] octopusArray;

        @MapRule(maxSize = 1,
                key = @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class)),
                value = @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class))
        )
        Map<Octopus, Octopus> octopusMap;

        @NestedDtoRule
        NestedDto nestedDto;

    }

    static class NestedDto {

        @CustomRule(generatorClass = OctopusKindGenerator.class, boundary = MIN_VALUE)
        String octopusKind;

        @CustomRule(generatorClass = OctopusKindGenerator.class)
        String octopusKindSecond;

        @CustomRule(generatorClass = OctopusGenerator.class, boundary = MIN_VALUE)
        Octopus octopus;

        @CustomRule(generatorClass = OctopusGenerator.class)
        Octopus octopusSecond;

        @CollectionRule(minSize = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class)))
        List<Octopus> octopusList;

        @ArrayRule(minLength = 10, element =
        @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class, boundary = MIN_VALUE)))
        Octopus[] octopusArray;

        @MapRule(maxSize = 1,
                key = @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class)),
                value = @Entry(customRule = @CustomRule(generatorClass = OctopusGenerator.class))
        )
        Map<Octopus, Octopus> octopusMap;

    }

    static class Octopus {
        int tentaclesNumber;

        public Octopus(int tentaclesNumber) {
            this.tentaclesNumber = tentaclesNumber;
        }
    }

    static class OctopusGenerator implements CustomGeneratorBoundary<Octopus> {

        Boundary boundary;

        @Override
        public Octopus generate() {
            int tentacles = 0;
                switch (boundary) {
                    case MIN_VALUE:
                        tentacles = 1;
                        break;
                    case MAX_VALUE:
                        tentacles = 8;
                        break;
                    case RANDOM_VALUE:
                    case NOT_DEFINED:
                        tentacles = RandomUtils.nextInt(2, 7);
                        break;
                    case NULL_VALUE:
                        return null;
                    default:
                        throw new IllegalStateException("Unexpected value: " + boundary);
                }
            return new Octopus(tentacles);
        }

        @Override
        public void setBoundary(Boundary boundary) {
            this.boundary = boundary;
        }
    }

    static class OctopusKindGenerator implements CustomGeneratorBoundary<String> {

        Boundary boundary;

        @Override
        public String generate() {

            String kind = "";
            switch (boundary) {
                case MIN_VALUE:
                    kind = KIND_MIN;
                    break;
                case MAX_VALUE:
                    kind = KIND_MAX;
                    break;
                case NOT_DEFINED:
                case RANDOM_VALUE:
                    kind = RandomUtils.nextString(10);
                    break;
                case NULL_VALUE:
                    kind = null;
            }
            return kind;
        }

        @Override
        public void setBoundary(Boundary boundary) {
            this.boundary = boundary;
        }
    }


    /*
     * Tests
     */

    static class OctopusMatcher extends TypeSafeMatcher<Octopus> {

        Integer tentaclesExpected;

        public OctopusMatcher(Integer expected) {
            this.tentaclesExpected = expected;
        }

        @Override
        protected boolean matchesSafely(Octopus item) {
            if (tentaclesExpected != null) {
                return item.tentaclesNumber == tentaclesExpected;
            }
            return item.tentaclesNumber > 1 && item.tentaclesNumber < 8;
        }

        @Override
        public void describeTo(Description description) {
        }

        public static OctopusMatcher octopusMatcher(int tentacles) {
            return new OctopusMatcher(tentacles);
        }

        public static OctopusMatcher maxOctopusMatcher() {
            return octopusMatcher(8);
        }

        public static OctopusMatcher minOctopusMatcher() {
            return octopusMatcher(1);
        }

        public static OctopusMatcher randomOctopusMatcher() {
            return new OctopusMatcher(null);
        }
    }

    @Test
    void customGeneratorWithRemarksAsIs() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.octopusKind, hasLength(10)),
                () -> assertThat(dto.octopusKindSecond, nullValue()),
                () -> assertThat(dto.octopus, randomOctopusMatcher()),
                () -> assertThat(dto.octopusSecond, maxOctopusMatcher()),
                () -> assertThat(dto.octopusList, everyItem(maxOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.octopusArray), everyItem(randomOctopusMatcher())),
                () -> assertThat(dto.octopusMap.keySet(), everyItem(randomOctopusMatcher())),
                () -> assertThat(dto.octopusMap.values(), everyItem(randomOctopusMatcher())),

                () -> assertThat(dto.nestedDto.octopusKind, equalTo(KIND_MIN)),
                () -> assertThat(dto.nestedDto.octopusKindSecond, hasLength(10)),
                () -> assertThat(dto.nestedDto.octopus, minOctopusMatcher()),
                () -> assertThat(dto.nestedDto.octopusSecond, randomOctopusMatcher()),
                () -> assertThat(dto.nestedDto.octopusList, everyItem(randomOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.nestedDto.octopusArray), everyItem(minOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap.keySet(), everyItem(randomOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap.values(), everyItem(randomOctopusMatcher()))
        );
    }

    @Test
    void customGeneratorWithRemarkAddedForAnyField() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .setBoundary(MAX_VALUE)
                .build().generateDto();

        assertAll(
                () -> assertThat(dto.octopusKind, equalTo(KIND_MAX)),
                () -> assertThat(dto.octopusKindSecond, equalTo(KIND_MAX)),
                () -> assertThat(dto.octopus, maxOctopusMatcher()),
                () -> assertThat(dto.octopusSecond, maxOctopusMatcher()),
                () -> assertThat(dto.octopusList, everyItem(maxOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.octopusArray), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto.octopusMap.keySet(), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto.octopusMap.values(), everyItem(maxOctopusMatcher())),

                () -> assertThat(dto.nestedDto.octopusKind, equalTo(KIND_MAX)),
                () -> assertThat(dto.nestedDto.octopusKindSecond, equalTo(KIND_MAX)),
                () -> assertThat(dto.nestedDto.octopus, maxOctopusMatcher()),
                () -> assertThat(dto.nestedDto.octopusSecond, maxOctopusMatcher()),
                () -> assertThat(dto.nestedDto.octopusList, everyItem(maxOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.nestedDto.octopusArray), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap.keySet(), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap.values(), everyItem(maxOctopusMatcher()))
        );
    }

    @Test
    void customGeneratorWithRemarksAddedByField() {

        // remarks set for root dto only

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setBoundary("octopusKind", MAX_VALUE)
                .setBoundary("octopusKindSecond", MIN_VALUE)
                .setBoundary("octopus", MAX_VALUE)
                .setBoundary("octopusSecond", MIN_VALUE)
                .setBoundary("octopusList", MIN_VALUE)
                .setBoundary("octopusArray", MAX_VALUE)
                .setBoundary("octopusMap", MIN_VALUE);

        Consumer<Dto> roodDtoAssertions = dto ->
                assertAll(
                        () -> assertThat(dto.octopusKind, equalTo(KIND_MAX)),
                        () -> assertThat(dto.octopusKindSecond, equalTo(KIND_MIN)),
                        () -> assertThat(dto.octopus.tentaclesNumber, equalTo(8)),
                        () -> assertThat(dto.octopusSecond.tentaclesNumber, equalTo(1)),
                        () -> assertThat(dto.octopusList, everyItem(minOctopusMatcher())),
                        () -> assertThat(Arrays.asList(dto.octopusArray), everyItem(maxOctopusMatcher())),
                        () -> assertThat(dto.octopusMap.keySet(), everyItem(minOctopusMatcher())),
                        () -> assertThat(dto.octopusMap.values(), everyItem(minOctopusMatcher()))
                );

        Dto dto = builder.build().generateDto();

        // root dto assertions
        roodDtoAssertions.accept(dto);

        // nested dto assertions
        assertAll(
                () -> assertThat(dto.nestedDto.octopusKind, equalTo(KIND_MIN)),
                () -> assertThat(dto.nestedDto.octopusKindSecond, hasLength(10)),
                () -> assertThat(dto.nestedDto.octopus.tentaclesNumber, equalTo(1)),
                () -> assertThat(dto.nestedDto.octopusSecond, randomOctopusMatcher()),
                () -> assertThat(dto.nestedDto.octopusList, everyItem(randomOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.nestedDto.octopusArray), everyItem(minOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap.keySet(), everyItem(randomOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap.values(), everyItem(randomOctopusMatcher()))
        );

        // remarks set for root dto and nested dto

        builder
                .setBoundary("nestedDto.octopusKind", MAX_VALUE)
                .setBoundary("nestedDto.octopusKindSecond", MIN_VALUE)
                .setBoundary("nestedDto.octopus", MAX_VALUE)
                .setBoundary("nestedDto.octopusSecond", MIN_VALUE)
                .setBoundary("nestedDto.octopusList", MIN_VALUE)
                .setBoundary("nestedDto.octopusArray", MAX_VALUE)
                .setBoundary("nestedDto.octopusMap", MIN_VALUE);

        Dto dto_2 = builder.build().generateDto();

        // root dto assertions
        roodDtoAssertions.accept(dto_2);

        // nested dto assertions
        assertAll(
                () -> assertThat(dto_2.nestedDto.octopusKind, equalTo(KIND_MAX)),
                () -> assertThat(dto_2.nestedDto.octopusKindSecond, equalTo(KIND_MIN)),
                () -> assertThat(dto_2.nestedDto.octopus.tentaclesNumber, equalTo(8)),
                () -> assertThat(dto_2.nestedDto.octopusSecond.tentaclesNumber, equalTo(1)),
                () -> assertThat(dto_2.nestedDto.octopusList, everyItem(minOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto_2.nestedDto.octopusArray), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto_2.nestedDto.octopusMap.keySet(), everyItem(minOctopusMatcher())),
                () -> assertThat(dto_2.nestedDto.octopusMap.values(), everyItem(minOctopusMatcher()))
        );
    }

    @Test
    void customGeneratorWithRemarksOverriddenByAnyAndSpecificFields() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .setBoundary(MAX_VALUE)
                .setBoundary("octopusKindSecond", MIN_VALUE)
                .setBoundary("octopusSecond", MIN_VALUE)
                .setBoundary("nestedDto.octopusArray", MIN_VALUE)
                .setBoundary("nestedDto.octopusKindSecond", MIN_VALUE)
                .setBoundary("nestedDto.octopus", NULL_VALUE)
                .setBoundary("nestedDto.octopusSecond", MIN_VALUE)
                .setBoundary("nestedDto.octopusMap", NULL_VALUE)
                .build().generateDto();

        assertAll(
                () -> assertThat(dto.octopusKind, equalTo(KIND_MAX)),
                () -> assertThat(dto.octopusKindSecond, equalTo(KIND_MIN)),
                () -> assertThat(dto.octopus.tentaclesNumber, equalTo(8)),
                () -> assertThat(dto.octopusSecond.tentaclesNumber, equalTo(1)),
                () -> assertThat(dto.octopusList, everyItem(maxOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.octopusArray), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto.octopusMap.keySet(), everyItem(maxOctopusMatcher())),
                () -> assertThat(dto.octopusMap.values(), everyItem(maxOctopusMatcher())),

                () -> assertThat(dto.nestedDto.octopusKind, equalTo(KIND_MAX)),
                () -> assertThat(dto.nestedDto.octopusKindSecond, equalTo(KIND_MIN)),
                () -> assertThat(dto.nestedDto.octopus, nullValue()),
                () -> assertThat(dto.nestedDto.octopusSecond.tentaclesNumber, equalTo(1)),
                () -> assertThat(dto.nestedDto.octopusList, everyItem(maxOctopusMatcher())),
                () -> assertThat(Arrays.asList(dto.nestedDto.octopusArray), everyItem(minOctopusMatcher())),
                () -> assertThat(dto.nestedDto.octopusMap, nullValue())
        );
    }

}
