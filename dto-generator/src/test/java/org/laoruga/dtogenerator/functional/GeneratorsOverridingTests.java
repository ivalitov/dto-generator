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
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersConfig;
import org.laoruga.dtogenerator.functional.data.dtoclient.ClientType;
import org.laoruga.dtogenerator.functional.util.TestUtils;
import org.laoruga.dtogenerator.generators.GeneratorBuilders;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.constants.BasicRuleRemark.MAX_VALUE;
import static org.laoruga.dtogenerator.constants.BasicRuleRemark.MIN_VALUE;

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
        LinkedList<ClientType> linkedListOfEnum;

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
        @DoubleRule
        List<Double> listOfDouble;

        @SetRule
        @IntegerRule
        Set<Integer> setOfInteger;

        @ListRule
        @EnumRule
        LinkedList<ClientType> linkedListOfEnum;

        Map<String, Integer> stringIntegerMap;

        public String getLocalDateTime() {
            return localDateTime.toString();
        }

        @Transient
        public LocalDateTime getLocalDateTimeAsIs() {
            return localDateTime;
        }

    }

    @Test
    @DisplayName("Overridden builders by annotations. General known type generators")
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
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.PERSON)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue())
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(2L)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(3D)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.PERSON)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new LinkedList<>(Arrays.asList(3D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new HashSet<>(Arrays.asList(1)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.PERSON)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue())
        );
    }

    @Test
    @DisplayName("Overridden builders by field name. General known type generators")
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
                .setCollectionGeneratorBuilder("linkedListOfEnum", GeneratorBuilders.setBuilder()
                        .elementGenerator(() -> ClientType.LEGAL_PERSON)
                        .collectionInstance(LinkedList::new)
                        .minSize(1).maxSize(1))

                .setGeneratorBuilder("innerDto.integer", GeneratorBuilders.integerBuilder().minValue(123).maxValue(123))
                .setGeneratorBuilder("innerDto.string", GeneratorBuilders.stringBuilder().minLength(3).maxLength(3).chars("f"))
                .setGeneratorBuilder("innerDto.aLong", GeneratorBuilders.longBuilder().minValue(4L).maxValue(4L))
                .setGeneratorBuilder("innerDto.aDouble", GeneratorBuilders.doubleBuilder().minValue(5D).maxValue(5D))
                .setGeneratorBuilder("innerDto.localDateTime", GeneratorBuilders.localDateTimeBuilder().leftShiftDays(1).rightShiftDays(-1))
                .setGeneratorBuilder("innerDto.clientType", GeneratorBuilders.enumBuilder().possibleEnumNames("ORG"))
                .setCollectionGeneratorBuilder("innerDto.listOfDouble", GeneratorBuilders.listBuilder()
                        .collectionInstance(CopyOnWriteArrayList::new)
                        .elementGenerator(() -> 99.99D)
                        .minSize(1).maxSize(1))
                .setCollectionGeneratorBuilder("innerDto.setOfInteger", GeneratorBuilders.setBuilder()
                        .elementGenerator(() -> 12345)
                        .collectionInstance(LinkedHashSet::new)
                        .minSize(1).maxSize(1))
                .setCollectionGeneratorBuilder("innerDto.linkedListOfEnum", GeneratorBuilders.setBuilder()
                        .elementGenerator(() -> ClientType.ORG)
                        .collectionInstance(LinkedList::new)
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

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("yyy")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(2L)))),
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.LEGAL_PERSON)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue())
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(123)),
                () -> assertThat(dto.getInnerDto().getString(), equalTo("fff")),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(4L)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(5D)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().minusDays(1))),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new CopyOnWriteArrayList<>(Arrays.asList(99.99D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new LinkedHashSet<>(Arrays.asList(12345)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue())
        );

    }

    @Test
    @DisplayName("Overridden configs of known type generators")
    void overriddenConfig() {
        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        TypeGeneratorBuildersConfig gensConfig = builder.getUserConfig().getGenBuildersConfig();

        gensConfig.setConfig(
                StringGenerator.ConfigDto.builder()
                        .minLength(1)
                        .maxLength(100)
                        .ruleRemark(MIN_VALUE)
                        .chars("x").build());

        gensConfig.setConfig(
                IntegerGenerator.ConfigDto.builder()
                        .minValue(-100)
                        .maxValue(1)
                        .ruleRemark(MAX_VALUE)
                        .build());

        gensConfig.setConfig(
                DoubleGenerator.ConfigDto.builder()
                        .minValue(2D)
                        .maxValue(100D)
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setConfig(
                LongGenerator.ConfigDto.builder()
                        .minValue(-100L)
                        .maxValue(3L)
                        .ruleRemark(MAX_VALUE)
                        .build());

        gensConfig.setConfig(
                LocalDateTimeGenerator.ConfigDto.builder()
                        .leftShiftDays(-1)
                        .rightShiftDays(100)
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setConfig(
                EnumGenerator.ConfigDto.builder()
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setCollectionConfig(
                List.class,
                CollectionGenerator.ConfigDto.builder()
                        .minSize(2)
                        .maxSize(100)
                        .collectionInstance(LinkedList::new)
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setCollectionConfig(
                Set.class,
                CollectionGenerator.ConfigDto.builder()
                        .minSize(0)
                        .maxSize(1)
                        .ruleRemark(MAX_VALUE)
                        .build());

        Dto dto = builder.build().generateDto();

        log.info(TestUtils.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("x")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getADouble(), equalTo(2D)),
                () -> assertThat(dto.getALong(), equalTo(3L)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().plusDays(1))),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("x", "x")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(3L)))),
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG, ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue())
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString(), equalTo("x")),
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(2D)),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(3L)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().plusDays(1))),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new LinkedList<>(Arrays.asList(2D, 2D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new HashSet<>(Arrays.asList(1)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG, ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue())
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
