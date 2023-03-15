package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.functional.data.dto.DtoAllKnownTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
@DisplayName("Builder reuse")
@Epic("BUILDER_REUSE")
@Slf4j
class BuilderReuseTests {

    @Test
    @DisplayName("Generating of two DTO instances")
    void generateToDtoInstances() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class);
        builder.getTypeGeneratorConfig().getCollectionConfig(List.class).setCollectionInstanceSupplier(LinkedList::new);

        DtoGenerator<DtoAllKnownTypes> dtoGenerator = builder.build();
        DtoAllKnownTypes dto_1 = dtoGenerator.generateDto();
        DtoAllKnownTypes dto_2 = dtoGenerator.generateDto();

        fieldsAssertions(dto_1);
        fieldsAssertions(dto_2);

        assertNotSame(dto_1, dto_2);

        assertAll(
                () -> assertNotEquals(dto_1, dto_2),
                () -> assertNotEquals(dto_1.hashCode(), dto_2.hashCode()),
                () -> assertNotEquals(dto_1.getString(), dto_2.getString()),
                () -> assertNotEquals(dto_1.getALong(), dto_2.getALong())
        );

    }

    @Test
    @DisplayName("Updating one DTO instance")
    void generateTwoVersionOfOneInstance() {

        DtoAllKnownTypes dto = new DtoAllKnownTypes();

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(dto);
        builder.getTypeGeneratorConfig().getCollectionConfig(List.class).setCollectionInstanceSupplier(LinkedList::new);

        DtoGenerator<DtoAllKnownTypes> dtoGenerator = builder.build();
        DtoAllKnownTypes dto_1 = dtoGenerator.generateDto();
        fieldsAssertions(dto_1);
        String stringFromDto1 = dto_1.getString();
        Long longFromDto1 = dto_1.getALong();

        DtoAllKnownTypes dto_2 = dtoGenerator.generateDto();
        fieldsAssertions(dto_2);
        String stringFromDto2 = dto_2.getString();
        Long longFromDto2 = dto_2.getALong();

        assertAll(
                () -> assertSame(dto, dto_1),
                () -> assertSame(dto_1, dto_2),
                () -> assertNotEquals(stringFromDto1, stringFromDto2),
                () -> assertNotEquals(longFromDto1, longFromDto2)
        );
    }

    @Test
    @DisplayName("Concurrent Execution")
    @SneakyThrows
    void concurrentExecution() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class);
        builder.getTypeGeneratorConfig().getCollectionConfig(List.class).setCollectionInstanceSupplier(LinkedList::new);

        DtoGenerator<DtoAllKnownTypes> dtoGenerator = builder.build();

        Callable<DtoAllKnownTypes> generateDto = dtoGenerator::generateDto;

        List<Callable<DtoAllKnownTypes>> generateDtoList = IntStream.range(0, 10).boxed()
                .map(i -> generateDto).collect(Collectors.toList());

        List<Future<DtoAllKnownTypes>> futures = Executors.newCachedThreadPool().invokeAll(generateDtoList);

        for (int i = 1; i < futures.size(); i++) {
            DtoAllKnownTypes dto_1 = futures.get(i - 1).get();
            DtoAllKnownTypes dto_2 = futures.get(i).get();

            assertNotSame(dto_1, dto_2);

            assertAll(
                    () -> assertNotEquals(dto_1, dto_2),
                    () -> assertNotEquals(dto_1.hashCode(), dto_2.hashCode()),
                    () -> assertNotEquals(dto_1.getString(), dto_2.getString()),
                    () -> assertNotEquals(dto_1.getALong(), dto_2.getALong())
            );
        }

    }

    private void fieldsAssertions(DtoAllKnownTypes dto) {
        LocalDate now = LocalDateTime.now().toLocalDate();
        assertAll(
                () -> assertThat(dto.getString().length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength())).and(
                        lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(dto.getInteger(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(
                        lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.getALong(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong())).and(
                        lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(dto.getADouble(), both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minDouble())).and(
                        lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxDouble()))),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(now)),
                () -> assertThat(dto.getClientType(), notNullValue()),

                () -> assertThat(dto.getListOfString().size(), both(
                        greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize()))),
                () -> assertThat(dto.getSetOfLong().size(), both(
                        greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize()))),
                () -> assertThat(dto.getLinkedListOfEnum().size(), both(
                        greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize()))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(888))
        );


        assertAll(
                () -> assertThat(dto.getString().length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength())).and(
                        lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(dto.getInteger(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(
                        lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.getALong(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong())).and(
                        lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(dto.getADouble(), both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minDouble())).and(
                        lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxDouble()))),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(now)),
                () -> assertThat(dto.getClientType(), notNullValue()),

                () -> assertThat(dto.getListOfString().size(), both(
                        greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize()))),
                () -> assertThat(dto.getSetOfLong().size(), both(
                        greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize()))),
                () -> assertThat(dto.getLinkedListOfEnum().size(), both(
                        greaterThanOrEqualTo(RulesInstance.COLLECTION_RULE.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.COLLECTION_RULE.maxSize()))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
        );
    }

}
