package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Value;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.Entry;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.generator.config.dto.CollectionConfig;

import java.time.Year;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.RuleRemark.MAX_VALUE;
import static org.laoruga.dtogenerator.constants.RuleRemark.MIN_VALUE;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("COLLECTION_RULES")
public class CollectionRuleTests {

    enum Planets {EARTH, SATURN, PLUTO, URANUS, MARS}

    static class Dto {

        @CollectionRule
        List<String> listOfString;

        @CollectionRule
        Set<Integer> setOfInteger;

        @CollectionRule
        Queue<Year> queueOfYear;

        @CollectionRule
        LinkedList<Double> linkedListOfDouble;

        @CollectionRule
        LinkedHashSet<AtomicInteger> linkedHashSetOfAtomicInteger;

        @CollectionRule
        ArrayDeque<Planets> arrayDequeOfEnum;

        @CollectionRule(minSize = 3, maxSize = 3, element = @Entry(customRule =
        @CustomRule(generatorClass = CustomGenerator.class, args = "SUPER_HERO")))
        List<CustomDto> listOfCustomDto;
    }

    @Test
    void annotationConfig() {

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, not(empty())),
                () -> assertThat(dto.setOfInteger, not(empty())),
                () -> assertThat(dto.queueOfYear, not(empty())),
                () -> assertThat(dto.linkedListOfDouble, not(empty())),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, not(empty())),
                () -> assertThat(dto.arrayDequeOfEnum, not(empty())),
                () -> assertThat(dto.listOfCustomDto, hasSize(3)),
                () -> assertThat(
                        dto.listOfCustomDto.stream().map(i -> i.argument).collect(Collectors.toList()),
                        everyItem(equalTo("SUPER_HERO")))
        );

    }

    @Test
    void listSetQueueConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier config = builder.getConfig().getTypeGeneratorsConfig();

        config.getCollectionConfig(List.class)
                .setMaxSize(0)
                .setMinSize(0);
        config.getCollectionConfig(Set.class)
                .setMaxSize(1)
                .setMinSize(1);
        config.getCollectionConfig(Queue.class)
                .setMaxSize(2)
                .setMinSize(2);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, empty()),
                () -> assertThat(dto.setOfInteger, hasSize(1)),
                () -> assertThat(dto.queueOfYear, hasSize(2)),
                // ambiguous config - List & Queue
                () -> assertThat(dto.linkedListOfDouble, notNullValue()),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, hasSize(1)),
                () -> assertThat(dto.arrayDequeOfEnum, hasSize(2)),
                () -> assertThat(dto.listOfCustomDto, empty())
        );
    }

    @Test
    void collectionConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.getConfig().getTypeGeneratorsConfig().getCollectionConfig(Collection.class)
                .setMaxSize(0)
                .setMinSize(0);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, empty()),
                () -> assertThat(dto.setOfInteger, empty()),
                () -> assertThat(dto.queueOfYear, empty()),
                () -> assertThat(dto.linkedListOfDouble, empty()),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, empty()),
                () -> assertThat(dto.arrayDequeOfEnum, empty()),
                () -> assertThat(dto.listOfCustomDto, empty())
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        staticConfig.getCollectionConfig(Collection.class)
                .setMinSize(1).setMaxSize(1);

        staticConfig.getCollectionConfig(LinkedHashSet.class)
                .setMinSize(2).setMaxSize(2);

        staticConfig.getCollectionConfig(ArrayDeque.class)
                .setMinSize(3).setMaxSize(3);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, hasSize(1)),
                () -> assertThat(dto.setOfInteger, hasSize(1)),
                () -> assertThat(dto.queueOfYear, hasSize(1)),
                () -> assertThat(dto.linkedListOfDouble, hasSize(1)),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, hasSize(2)),
                () -> assertThat(dto.arrayDequeOfEnum, hasSize(3)),
                () -> assertThat(dto.listOfCustomDto, hasSize(1))
        );
    }

    @Test
    void instanceConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();

        instanceConfig.getCollectionConfig(Collection.class)
                .setMinSize(1).setMaxSize(1);

        instanceConfig.getCollectionConfig(LinkedHashSet.class)
                .setMinSize(2).setMaxSize(2);

        instanceConfig.getCollectionConfig(ArrayDeque.class)
                .setMinSize(3).setMaxSize(3);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, hasSize(1)),
                () -> assertThat(dto.setOfInteger, hasSize(1)),
                () -> assertThat(dto.queueOfYear, hasSize(1)),
                () -> assertThat(dto.linkedListOfDouble, hasSize(1)),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, hasSize(2)),
                () -> assertThat(dto.arrayDequeOfEnum, hasSize(3)),
                () -> assertThat(dto.listOfCustomDto, hasSize(1))
        );
    }

    @Test
    void fieldConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setTypeGeneratorConfig("listOfString",
                        CollectionConfig.builder().minSize(0).maxSize(0).build())
                .setTypeGeneratorConfig("setOfInteger",
                        CollectionConfig.builder().minSize(2).maxSize(2).build())
                .setTypeGeneratorConfig("queueOfYear",
                        CollectionConfig.builder().minSize(3).maxSize(3).build())
                .setTypeGeneratorConfig("linkedListOfDouble",
                        CollectionConfig.builder().minSize(4).maxSize(4).build())
                .setTypeGeneratorConfig("linkedHashSetOfAtomicInteger",
                        CollectionConfig.builder().minSize(5).maxSize(5).build())
                .setTypeGeneratorConfig("arrayDequeOfEnum",
                        CollectionConfig.builder().minSize(6).maxSize(6).build())
                .setTypeGeneratorConfig("listOfCustomDto",
                        CollectionConfig.builder().minSize(7).maxSize(7).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, hasSize(0)),
                () -> assertThat(dto.setOfInteger, hasSize(2)),
                () -> assertThat(dto.queueOfYear, hasSize(3)),
                () -> assertThat(dto.linkedListOfDouble, hasSize(4)),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, hasSize(5)),
                () -> assertThat(dto.arrayDequeOfEnum, hasSize(6)),
                () -> assertThat(dto.listOfCustomDto, hasSize(7))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldAndAnnotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        // static
        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        staticConfig.getCollectionConfig(Collection.class)
                .setMaxSize(5);

        staticConfig.getCollectionConfig(LinkedHashSet.class)
                .setRuleRemark(MAX_VALUE);

        staticConfig.getCollectionConfig(ArrayDeque.class)
                .setMinSize(3);

        // instance
        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();

        instanceConfig.getCollectionConfig(Collection.class)
                .setRuleRemark(MAX_VALUE);

        instanceConfig.getCollectionConfig(LinkedHashSet.class)
                .setMaxSize(4);


        // field
        builder
                .setTypeGeneratorConfig("setOfInteger",
                        CollectionConfig.builder().ruleRemark(MIN_VALUE).build())
                .setTypeGeneratorConfig("queueOfYear",
                        CollectionConfig.builder().maxSize(2).minSize(2).build())
                .setTypeGeneratorConfig("linkedListOfDouble",
                        CollectionConfig.builder().minSize(4).maxSize(4).build())
                .setTypeGeneratorConfig("linkedHashSetOfAtomicInteger",
                        CollectionConfig.builder().minSize(0).build())
                .setTypeGeneratorConfig("arrayDequeOfEnum",
                        CollectionConfig.builder().ruleRemark(MIN_VALUE).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Static + instance", dto.listOfString, hasSize(5)),
                () -> assertThat("Static + field", dto.setOfInteger, hasSize(1)),
                () -> assertThat("Instance + field", dto.queueOfYear, hasSize(2)),
                () -> assertThat("Instance + field", dto.linkedListOfDouble, hasSize(4)),
                () -> assertThat("Static + instance", dto.linkedHashSetOfAtomicInteger, hasSize(4)),
                () -> assertThat("Static + field", dto.arrayDequeOfEnum, hasSize(3)),
                () -> assertThat("Static + field", dto.listOfCustomDto, hasSize(5))
        );
    }

    @Test
    void overrideGeneratorByField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        final List<String> listOfString = new ArrayList<>();
        final Set<Integer> setOfInteger = new HashSet<>();
        final Queue<Year> queueOfYear = new PriorityQueue<>();
        final LinkedList<Double> linkedListOfDouble = new LinkedList<>();
        final LinkedHashSet<AtomicInteger> linkedHashSetOfAtomicInteger = new LinkedHashSet<>();
        final ArrayDeque<Planets> arrayDequeOfEnum = new ArrayDeque<>();
        final List<CustomDto> listOfCustomDto = new CopyOnWriteArrayList<>();

        builder
                .setGenerator("listOfString", () -> listOfString)
                .setGenerator("setOfInteger", () -> setOfInteger)
                .setGenerator("queueOfYear", () -> queueOfYear)
                .setGenerator("linkedListOfDouble", () -> linkedListOfDouble)
                .setGenerator("linkedHashSetOfAtomicInteger", () -> linkedHashSetOfAtomicInteger)
                .setGenerator("arrayDequeOfEnum", () -> arrayDequeOfEnum)
                .setGenerator("listOfCustomDto", () -> listOfCustomDto);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, sameInstance(listOfString)),
                () -> assertThat(dto.setOfInteger, sameInstance(setOfInteger)),
                () -> assertThat(dto.queueOfYear, sameInstance(queueOfYear)),
                () -> assertThat(dto.linkedListOfDouble, sameInstance(linkedListOfDouble)),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, sameInstance(linkedHashSetOfAtomicInteger)),
                () -> assertThat(dto.arrayDequeOfEnum, sameInstance(arrayDequeOfEnum)),
                () -> assertThat(dto.listOfCustomDto, sameInstance(listOfCustomDto))
        );
    }

    @Test
    void overrideGeneratorByType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        final List<String> listOfString = new ArrayList<>();
        final Set<Integer> setOfInteger = new HashSet<>();
        final Queue<Year> queueOfYear = new PriorityQueue<>();
        final LinkedList<Double> linkedListOfDouble = new LinkedList<>();
        final LinkedHashSet<AtomicInteger> linkedHashSetOfAtomicInteger = new LinkedHashSet<>();
        final ArrayDeque<Planets> arrayDequeOfEnum = new ArrayDeque<>();

        builder
                .setGenerator(List.class, () -> listOfString)
                .setGenerator(Set.class, () -> setOfInteger)
                .setGenerator(Queue.class, () -> queueOfYear)
                .setGenerator(LinkedList.class, () -> linkedListOfDouble)
                .setGenerator(LinkedHashSet.class, () -> linkedHashSetOfAtomicInteger)
                .setGenerator(ArrayDeque.class, () -> arrayDequeOfEnum);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, sameInstance(listOfString)),
                () -> assertThat(dto.setOfInteger, sameInstance(setOfInteger)),
                () -> assertThat(dto.queueOfYear, sameInstance(queueOfYear)),
                () -> assertThat(dto.linkedListOfDouble, sameInstance(linkedListOfDouble)),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, sameInstance(linkedHashSetOfAtomicInteger)),
                () -> assertThat(dto.arrayDequeOfEnum, sameInstance(arrayDequeOfEnum)),
                () -> assertThat(dto.listOfCustomDto, sameInstance(listOfString))
        );
    }

    @Test
    void overrideGeneratorByTypeAndField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        final List<String> listOfString = new ArrayList<>();
        final Set<Integer> setOfInteger = new HashSet<>();
        final Queue<Year> queueOfYear = new PriorityQueue<>();
        final LinkedList<Double> linkedListOfDouble = new LinkedList<>();
        final LinkedHashSet<AtomicInteger> linkedHashSetOfAtomicInteger = new LinkedHashSet<>();
        final ArrayDeque<Planets> arrayDequeOfEnum = new ArrayDeque<>();

        builder
                .setGenerator(List.class, () -> listOfString)
                .setGenerator(Set.class, () -> setOfInteger)
                .setGenerator(Queue.class, () -> queueOfYear)
                .setGenerator("linkedListOfDouble", () -> linkedListOfDouble)
                .setGenerator("linkedHashSetOfAtomicInteger", () -> linkedHashSetOfAtomicInteger)
                .setGenerator("arrayDequeOfEnum", () -> arrayDequeOfEnum);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, sameInstance(listOfString)),
                () -> assertThat(dto.setOfInteger, sameInstance(setOfInteger)),
                () -> assertThat(dto.queueOfYear, sameInstance(queueOfYear)),
                () -> assertThat(dto.linkedListOfDouble, sameInstance(linkedListOfDouble)),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, sameInstance(linkedHashSetOfAtomicInteger)),
                () -> assertThat(dto.arrayDequeOfEnum, sameInstance(arrayDequeOfEnum))
        );
    }

    static class Dto_2 {

        List<String> listOfString;
        Set<Integer> setOfInteger;
        Queue<Year> queueOfYear;
        LinkedList<Double> linkedListOfDouble;
        LinkedHashSet<AtomicInteger> linkedHashSetOfAtomicInteger;
        ArrayDeque<Planets> arrayDequeOfEnum;
        List<CustomDto> listOfCustomDto;

    }

    @Test
    void withoutAnnotations() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, not(empty())),
                () -> assertThat(dto.setOfInteger, not(empty())),
                () -> assertThat(dto.queueOfYear, not(empty())),
                () -> assertThat(dto.linkedListOfDouble, not(empty())),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, not(empty())),
                () -> assertThat(dto.arrayDequeOfEnum, not(empty())),
                () -> assertThat(dto.listOfCustomDto, nullValue())
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void withoutAnnotationsWithOverriddenConfig() {
        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        // static
        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        staticConfig.getCollectionConfig(Collection.class)
                .setMaxSize(5);

        staticConfig.getCollectionConfig(LinkedHashSet.class)
                .setRuleRemark(MAX_VALUE);

        staticConfig.getCollectionConfig(ArrayDeque.class)
                .setMinSize(3);

        // instance
        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();

        instanceConfig.getCollectionConfig(Collection.class)
                .setRuleRemark(MAX_VALUE);

        instanceConfig.getCollectionConfig(LinkedHashSet.class)
                .setMaxSize(4);


        // field
        builder
                .setTypeGeneratorConfig("setOfInteger",
                        CollectionConfig.builder().ruleRemark(MIN_VALUE).build())
                .setTypeGeneratorConfig("queueOfYear",
                        CollectionConfig.builder().maxSize(2).minSize(2).build())
                .setTypeGeneratorConfig("linkedListOfDouble",
                        CollectionConfig.builder().minSize(4).maxSize(4).build())
                .setTypeGeneratorConfig("linkedHashSetOfAtomicInteger",
                        CollectionConfig.builder().minSize(0).build())
                .setTypeGeneratorConfig("arrayDequeOfEnum",
                        CollectionConfig.builder().ruleRemark(MIN_VALUE).build())
                .setGenerator("listOfCustomDto", ArrayList::new);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Static + instance", dto.listOfString, hasSize(5)),
                () -> assertThat("Static + field", dto.setOfInteger, hasSize(1)),
                () -> assertThat("Instance + field", dto.queueOfYear, hasSize(2)),
                () -> assertThat("Instance + field", dto.linkedListOfDouble, hasSize(4)),
                () -> assertThat("Static + instance", dto.linkedHashSetOfAtomicInteger, hasSize(4)),
                () -> assertThat("Static + field", dto.arrayDequeOfEnum, hasSize(3)),
                () -> assertThat("Generator", dto.listOfCustomDto, hasSize(0))
        );

    }


    @Value
    static class CustomDto {
        String argument;
    }

    static class CustomGenerator implements CustomGeneratorArgs<CustomDto> {

        String arg;

        @Override
        public CustomDto generate() {
            return new CustomDto(arg);
        }

        @Override
        public void setArgs(String[] args) {
            arg = args[0];
        }
    }

}
