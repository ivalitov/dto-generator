package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.functional.data.dtoclient.ClientType;
import org.laoruga.dtogenerator.functional.util.TestUtils;
import org.laoruga.dtogenerator.generators.GeneratorBuilders;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 02.07.2022
 */
@DisplayName("Overriding of generators")
@Epic("GENERATORS_OVERRIDING")
@Slf4j
class GeneratorsOverridingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @StringRule
        String string;

        @IntegerRule
        Integer integer;

        @LongRule
        Long aLong;

        @DoubleRule
        Double aDouble;

        @LocalDateTimeRule
        LocalDateTime localDateTime;

        @EnumRule
        ClientType clientType;

        @ListRule
        @StringRule
        List<String> listOfString;

        @SetRule
        @LongRule
        Set<Long> setOfLong;

        @ListRule
        @EnumRule
        ArrayList<ClientType> linkedListOfEnum;

        @NestedDtoRule
        InnerDto innerDto;

        Map<String, Integer> stringIntegerMap;

        public String getLocalDateTime() {
            return localDateTime.toString();
        }

        @Transient
        public LocalDateTime getLocalDateTimeAsIs() {
            return localDateTime;
        }
    }

    @Getter
    @NoArgsConstructor
    static class InnerDto {
        @StringRule
        String string;

        @IntegerRule
        Integer innerInteger;

        @StringRule
        String innerString;
    }

    @Test
    @DisplayName("Overridden builders by annotations. General type generators")
    void
    basicGeneratorOverridden() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setGeneratorBuilder(StringRule.class, GeneratorBuilders.stringBuilder().minLength(5).maxLength(5).chars("x"))
                .setGeneratorBuilder(IntegerRule.class, GeneratorBuilders.integerBuilder().minValue(1).maxValue(1))
                .setGeneratorBuilder(LongRule.class, GeneratorBuilders.longBuilder().minValue(2L).maxValue(2L))
                .setGeneratorBuilder(DoubleRule.class, GeneratorBuilders.doubleBuilder().minValue(3D).maxValue(3D))
                .setGeneratorBuilder(LocalDateTimeRule.class, GeneratorBuilders.localDateTimeBuilder().leftShiftDays(0).rightShiftDays(0))
                .setGeneratorBuilder(EnumRule.class, GeneratorBuilders.enumBuilder().possibleEnumNames("PERSON"))
                .setCollectionGeneratorBuilder(ListRule.class, GeneratorBuilders.listBuilder()
                        .collectionInstance(LinkedList::new)
                        .minSize(1).maxSize(1))
                .setCollectionGeneratorBuilder(SetRule.class, GeneratorBuilders.setBuilder()
                        .collectionInstance(HashSet::new)
                        .minSize(1).maxSize(1));

        Dto dto = builder.build().generateDto();

        log.info(TestUtils.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getALong(), equalTo(2L)),
                () -> assertThat(dto.getADouble(), equalTo(3D)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.PERSON)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("xxxxx")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(2L)))),

                () -> assertThat(dto.getInnerDto().getInnerInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getInnerString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInnerDto().getString(), equalTo("xxxxx"))
        );
    }

    @Test
    @DisplayName("Overridden builders by field name. General type generators")
    void fieldGeneratorOverridden() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class)
                .setGeneratorBuilder("string", GeneratorBuilders.stringBuilder().minLength(5).maxLength(5).chars("x"))
                .setGeneratorBuilder("integer", GeneratorBuilders.integerBuilder().minValue(1).maxValue(1))
                .setGeneratorBuilder("aLong", GeneratorBuilders.longBuilder().minValue(2L).maxValue(2L))
                .setGeneratorBuilder("aDouble", GeneratorBuilders.doubleBuilder().minValue(3D).maxValue(3D))
                .setGeneratorBuilder("localDateTime", GeneratorBuilders.localDateTimeBuilder().leftShiftDays(0).rightShiftDays(0))
                .setGeneratorBuilder("clientType", GeneratorBuilders.enumBuilder().possibleEnumNames("PERSON"))
                .setCollectionGeneratorBuilder("listOfString", GeneratorBuilders.listBuilder()
                        .collectionInstance(LinkedList::new)
                        .elementGenerator(() -> "yyy")
                        .minSize(1).maxSize(1))
                .setCollectionGeneratorBuilder("setOfLong", GeneratorBuilders.setBuilder()
                        .elementGenerator(() -> 2L)
                        .collectionInstance(HashSet::new)
                        .minSize(1).maxSize(1))

                .setGeneratorBuilder("stringIntegerMap", () -> () -> {
                    Map<String, Integer> map = new HashMap<>();
                    map.put("1", 1);
                    return map;
                })

                .setGeneratorBuilder("innerDto.innerInteger", GeneratorBuilders.integerBuilder().minValue(123).maxValue(123))
                .setGeneratorBuilder("innerDto.innerString", () -> () -> "fff");

        Dto dto = builder.build().generateDto();

        log.info(TestUtils.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getALong(), equalTo(2L)),
                () -> assertThat(dto.getADouble(), equalTo(3D)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.PERSON)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("yyy")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(2L)))),

                () -> assertThat(dto.getInnerDto().getInnerInteger(), equalTo(123)),
                () -> assertThat(dto.getInnerDto().getInnerString(), equalTo("fff")),
                () -> assertThat(dto.getInnerDto().getString(), not(equalTo("xxxxx")))
        );
    }

    static class NumberGenerator implements ICustomGeneratorArgs<Integer> {
        int generated;

        @Override
        public NumberGenerator setArgs(String... args) {
            generated = Integer.parseInt(args[0]);
            return this;
        }

        @Override
        public Integer generate() {
            return generated;
        }
    }

}
