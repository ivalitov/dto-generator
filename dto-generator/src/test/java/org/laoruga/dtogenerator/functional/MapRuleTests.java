package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.Entry;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.Configuration;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;
import org.laoruga.dtogenerator.generator.config.dto.MapConfig;
import org.laoruga.dtogenerator.generator.config.dto.datetime.ChronoUnitConfig;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Boolean.TRUE;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.Boundary.*;
import static org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType.ORG;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("MAP_RULES")
public class MapRuleTests {

    static class Dto {

        @MapRule(
                key = @Entry(stringRule = @StringRule(maxLength = 10)),
                minSize = 10
        )
        Map<String, Integer> stringIntegerMap;

        @MapRule(minSize = 3, maxSize = 3)
        HashMap<ClientType, Long> enumLongHashMap;

        @MapRule
        TreeMap<Double, LocalDateTime> doubleLocalDateTimeTreeMap;

        @MapRule(
                key = @Entry(dateTimeRule = @DateTimeRule(chronoUnitShift = @ChronoUnitShift(unit = YEARS, leftBound = -100))),
                mapClass = ConcurrentHashMap.class
        )
        Map<Year, Boolean> yearBooleanMap;

        @MapRule(minSize = 1, maxSize = 1,
                key = @Entry(customRule = @CustomRule(generatorClass = CustomGenerator.class, args = "MARIO")),
                value = @Entry(customRule = @CustomRule(generatorClass = CustomGenerator.class, args = "LUIGI"))
        )
        Map<CustomDto, CustomDto> customDtoOfCustomDto;

    }

    final CustomDto CUSTOM_DTO_MARIO = new CustomDto("MARIO");
    final CustomDto CUSTOM_DTO_LUIGI = new CustomDto("LUIGI");

    @Test
    void annotationConfig() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap.size(), greaterThanOrEqualTo(10)),
                () -> assertThat(dto.stringIntegerMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.enumLongHashMap.size(), equalTo(3)),
                () -> assertThat(dto.enumLongHashMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.doubleLocalDateTimeTreeMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.yearBooleanMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto.yearBooleanMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.customDtoOfCustomDto.size(), equalTo(1)),
                () -> assertThat(dto.customDtoOfCustomDto.get(CUSTOM_DTO_MARIO), equalTo(CUSTOM_DTO_LUIGI))
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        staticConfig.getMapConfig(Map.class)
                .setMapInstanceSupplier(LinkedHashMap::new)
                .setMinSize(1)
                .setMaxSize(1);

        staticConfig.getMapConfig(HashMap.class)
                .setMinSize(2)
                .setMaxSize(2);

        staticConfig.getMapConfig(TreeMap.class)
                .setRuleRemark(MAX_VALUE);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.stringIntegerMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto.stringIntegerMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.enumLongHashMap.size(), equalTo(2)),
                () -> assertThat(dto.enumLongHashMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.doubleLocalDateTimeTreeMap.size(), equalTo(10)),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.yearBooleanMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.yearBooleanMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto.yearBooleanMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.customDtoOfCustomDto.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.customDtoOfCustomDto.size(), equalTo(1)),
                () -> assertThat(dto.customDtoOfCustomDto.get(CUSTOM_DTO_MARIO), equalTo(CUSTOM_DTO_LUIGI))
        );

        // Map.class config - the same

        staticConfig.getMapConfig(HashMap.class)
                .setKeyGenerator(() -> ORG)
                .setMinSize(1)
                .setMaxSize(1);

        LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now().minusYears(1000);
        staticConfig.getMapConfig(TreeMap.class)
                .setValueGenerator(() -> LOCAL_DATE_TIME)
                .setRuleRemark(MAX_VALUE);

        // next dto instance
        Dto dto2 = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto2.stringIntegerMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto2.stringIntegerMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto2.stringIntegerMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto2.enumLongHashMap.size(), equalTo(1)),
                () -> assertThat(dto2.enumLongHashMap.keySet(), everyItem(equalTo(ORG))),
                () -> assertThat(dto2.enumLongHashMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto2.doubleLocalDateTimeTreeMap.size(), equalTo(10)),
                () -> assertThat(dto2.doubleLocalDateTimeTreeMap.values(), everyItem(equalTo(LOCAL_DATE_TIME))),

                () -> assertThat(dto2.yearBooleanMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto2.yearBooleanMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto2.yearBooleanMap.values(), everyItem(notNullValue())),


                () -> assertThat(dto.customDtoOfCustomDto.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.customDtoOfCustomDto.size(), equalTo(1)),
                () -> assertThat(dto.customDtoOfCustomDto.get(CUSTOM_DTO_MARIO), equalTo(CUSTOM_DTO_LUIGI))
        );
    }

    @Test
    void instanceConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();

        instanceConfig.getMapConfig(Map.class)
                .setMapInstanceSupplier(LinkedHashMap::new)
                .setMinSize(1)
                .setMaxSize(1);

        instanceConfig.getMapConfig(HashMap.class)
                .setMinSize(2)
                .setMaxSize(2);

        instanceConfig.getMapConfig(TreeMap.class)
                .setRuleRemark(MAX_VALUE);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.stringIntegerMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto.stringIntegerMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.enumLongHashMap.size(), equalTo(2)),
                () -> assertThat(dto.enumLongHashMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.doubleLocalDateTimeTreeMap.size(), equalTo(10)),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.yearBooleanMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.yearBooleanMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto.yearBooleanMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.customDtoOfCustomDto.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto.customDtoOfCustomDto.size(), equalTo(1)),
                () -> assertThat(dto.customDtoOfCustomDto.get(CUSTOM_DTO_MARIO), equalTo(CUSTOM_DTO_LUIGI))
        );

        // Map.class config - the same

        instanceConfig.getMapConfig(HashMap.class)
                .setKeyGenerator(() -> ORG)
                .setMinSize(1)
                .setMaxSize(1);

        LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now().minusYears(1000);
        instanceConfig.getMapConfig(TreeMap.class)
                .setValueGenerator(() -> LOCAL_DATE_TIME)
                .setRuleRemark(MAX_VALUE);

        // next dto instance
        Dto dto2 = builder.build().generateDto();


        assertAll(
                () -> assertThat(dto2.stringIntegerMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto2.stringIntegerMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto2.stringIntegerMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto2.enumLongHashMap.size(), equalTo(1)),
                () -> assertThat(dto2.enumLongHashMap.keySet(), everyItem(equalTo(ORG))),
                () -> assertThat(dto2.enumLongHashMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto2.doubleLocalDateTimeTreeMap.size(), equalTo(10)),
                () -> assertThat(dto2.doubleLocalDateTimeTreeMap.values(), everyItem(equalTo(LOCAL_DATE_TIME))),

                () -> assertThat(dto2.yearBooleanMap.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto2.yearBooleanMap.size(), greaterThanOrEqualTo(1)),
                () -> assertThat(dto2.yearBooleanMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto2.customDtoOfCustomDto.getClass(), equalTo(LinkedHashMap.class)),
                () -> assertThat(dto2.customDtoOfCustomDto.size(), equalTo(1)),
                () -> assertThat(dto2.customDtoOfCustomDto.get(CUSTOM_DTO_MARIO), equalTo(CUSTOM_DTO_LUIGI))
        );
    }

    @Test
    void fieldConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        LinkedHashMap<Object, Object> objectObjectLinkedHashMap = new LinkedHashMap<>();

        builder
                .setGeneratorConfig("stringIntegerMap",
                        MapConfig.builder().maxSize(10).build())
                .setGeneratorConfig("enumLongHashMap",
                        MapConfig.builder().minSize(1).maxSize(1).build())
                .setGeneratorConfig("doubleLocalDateTimeTreeMap",
                        MapConfig.builder().ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("yearBooleanMap",
                        MapConfig.builder().mapInstanceSupplier(() -> objectObjectLinkedHashMap).build())
                .setGeneratorConfig("customDtoOfCustomDto",
                        MapConfig.builder().minSize(0).maxSize(0).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap.size(), equalTo(10)),
                () -> assertThat(dto.stringIntegerMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.enumLongHashMap.size(), equalTo(1)),
                () -> assertThat(dto.enumLongHashMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.doubleLocalDateTimeTreeMap.size(), equalTo(10)),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.yearBooleanMap, sameInstance(objectObjectLinkedHashMap)),
                () -> assertThat(dto.yearBooleanMap.values(), everyItem(notNullValue())),

                () -> assertThat(dto.customDtoOfCustomDto.size(), equalTo(0))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldAndAnnotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        // static
        Configuration staticConfig = builder.getStaticConfig();

        staticConfig.getTypeGeneratorsConfig().getMapConfig(Map.class)
                .setMinSize(0)
                .setMaxSize(20);

        staticConfig.getTypeGeneratorsConfig().getMapConfig(HashMap.class)
                .setMinSize(1);

        staticConfig.getTypeGeneratorsConfig().getMapConfig(TreeMap.class)
                .setRuleRemark(MAX_VALUE);

        // instance
        builder.getConfig().getTypeGeneratorsConfig().getMapConfig(Map.class)
                .setRuleRemark(MAX_VALUE);

        builder.getConfig().getTypeGeneratorsConfig().getMapConfig(HashMap.class)
                .setRuleRemark(MIN_VALUE);

        builder.getConfig().getTypeGeneratorsConfig().getMapConfig(TreeMap.class)
                .setKeyGenerator(() -> 1D);


        // field
        builder
                .setGeneratorConfig("stringIntegerMap",
                        MapConfig.builder().maxSize(15).build())

                .setGeneratorConfig("enumLongHashMap",
                        MapConfig.builder().valueGenerator(() -> 1L).build())

                .setGeneratorConfig("doubleLocalDateTimeTreeMap",
                        MapConfig.builder().minSize(1).maxSize(1).build())

                .setGeneratorConfig("yearBooleanMap",
                        MapConfig.builder().minSize(20).ruleRemark(RANDOM_VALUE).valueGenerator(() -> TRUE).build())

                .setGenerator("customDtoOfCustomDto",
                        HashMap::new);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Instance + field - 1", +dto.stringIntegerMap.size(), equalTo(15)),
                () -> assertThat("Instance + field - 1", dto.stringIntegerMap.values(), everyItem(notNullValue())),
                () -> assertThat("Static + instance + field - 1", dto.enumLongHashMap.size(), equalTo(1)),
                () -> assertThat("Static + instance + field - 1", dto.enumLongHashMap.values(), everyItem(equalTo(1L))),
                () -> assertThat("Instance + field - 2", dto.doubleLocalDateTimeTreeMap.size(), equalTo(1)),
                () -> assertThat("Instance + field - 2", dto.doubleLocalDateTimeTreeMap.keySet(), everyItem(equalTo(1D))),
                () -> assertThat("Static + Instance + field - 2", dto.yearBooleanMap.size(), equalTo(20)),
                () -> assertThat("Static + Instance + field - 2", dto.yearBooleanMap.values(), everyItem(equalTo(TRUE))),
                () -> assertThat("Overridden generator", dto.customDtoOfCustomDto, notNullValue())
        );
    }

    @Test
    void overrideGeneratorByField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        Map<String, Integer> stringIntegerMap = new LinkedHashMap<>();
        HashMap<ClientType, Long> enumLongHashMap = new HashMap<>();
        TreeMap<Double, LocalDateTime> doubleLocalDateTimeTreeMap = new TreeMap<>();
        Map<Year, Boolean> yearBooleanMap = new ConcurrentHashMap<>();
        Map<CustomDto, CustomDto> customDtoOfCustomDto = new ConcurrentHashMap<>();


        builder
                .setGenerator("stringIntegerMap", () -> stringIntegerMap)
                .setGenerator("enumLongHashMap", () -> enumLongHashMap)
                .setGenerator("doubleLocalDateTimeTreeMap", () -> doubleLocalDateTimeTreeMap)
                .setGenerator("yearBooleanMap", () -> yearBooleanMap)
                .setGenerator("customDtoOfCustomDto", () -> customDtoOfCustomDto);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap, sameInstance(stringIntegerMap)),
                () -> assertThat(dto.enumLongHashMap, sameInstance(enumLongHashMap)),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap, sameInstance(doubleLocalDateTimeTreeMap)),
                () -> assertThat(dto.yearBooleanMap, sameInstance(yearBooleanMap)),
                () -> assertThat(dto.customDtoOfCustomDto, sameInstance(customDtoOfCustomDto))
        );
    }

    @Test
    void overrideGeneratorByType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        Map<String, Integer> stringIntegerMap = new LinkedHashMap<>();
        HashMap<ClientType, Long> enumLongHashMap = new HashMap<>();
        TreeMap<Double, LocalDateTime> doubleLocalDateTimeTreeMap = new TreeMap<>();

        builder.setGenerator(Map.class, () -> stringIntegerMap);
        builder.setGenerator(HashMap.class, () -> enumLongHashMap);
        builder.setGenerator(TreeMap.class, () -> doubleLocalDateTimeTreeMap);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap, sameInstance(stringIntegerMap)),
                () -> assertThat(dto.enumLongHashMap, sameInstance(enumLongHashMap)),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap, sameInstance(doubleLocalDateTimeTreeMap)),
                () -> assertThat(dto.yearBooleanMap, sameInstance(stringIntegerMap)),
                () -> assertThat(dto.customDtoOfCustomDto, sameInstance(stringIntegerMap))
        );
    }

    static class Dto_2 {

        Map<String, Integer> stringIntegerMap;

        HashMap<ClientType, Long> enumLongHashMap;

        TreeMap<Double, LocalDateTime> doubleLocalDateTimeTreeMap;

        Map<Year, Boolean> yearBooleanMap;

        Map<CustomDto, CustomDto> customDtoOfCustomDto;
    }

    @Test
    void withoutAnnotations() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.stringIntegerMap.values(), everyItem(notNullValue())),
                () -> assertThat(dto.enumLongHashMap.values(), everyItem(notNullValue())),
                () -> assertThat(dto.doubleLocalDateTimeTreeMap.values(), everyItem(notNullValue())),
                () -> assertThat(dto.yearBooleanMap.values(), everyItem(notNullValue())),
                () -> assertThat(dto.customDtoOfCustomDto, nullValue())
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void withoutAnnotationsWithOverriddenConfig() {


        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        // static
        Configuration staticConfig = builder.getStaticConfig();

        staticConfig.getTypeGeneratorsConfig().getMapConfig(Map.class)
                .setMinSize(0)
                .setMaxSize(20);

        staticConfig.getTypeGeneratorsConfig().getMapConfig(HashMap.class)
                .setMinSize(1);

        staticConfig.getTypeGeneratorsConfig().getMapConfig(TreeMap.class)
                .setRuleRemark(MAX_VALUE);

        staticConfig.getTypeGeneratorsConfig().getDateTimeConfig(Year.class)
                .addChronoConfig(ChronoUnitConfig.newBounds(-100, 100, YEARS));

        // instance
        builder.getConfig().getTypeGeneratorsConfig().getMapConfig(Map.class)
                .setRuleRemark(MAX_VALUE);

        builder.getConfig().getTypeGeneratorsConfig().getMapConfig(HashMap.class)
                .setRuleRemark(MIN_VALUE);

        builder.getConfig().getTypeGeneratorsConfig().getMapConfig(TreeMap.class)
                .setKeyGenerator(() -> 1D);


        // field
        builder
                .setGeneratorConfig("stringIntegerMap",
                        MapConfig.builder().maxSize(15).build())

                .setGeneratorConfig("enumLongHashMap",
                        MapConfig.builder().valueGenerator(() -> 1L).build())

                .setGeneratorConfig("doubleLocalDateTimeTreeMap",
                        MapConfig.builder().minSize(1).maxSize(1).build())

                .setGeneratorConfig("yearBooleanMap",
                        MapConfig.builder().minSize(20).ruleRemark(RANDOM_VALUE).valueGenerator(() -> TRUE).build());


        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Instance + field - 1", +dto.stringIntegerMap.size(), equalTo(15)),
                () -> assertThat("Instance + field - 1", dto.stringIntegerMap.values(), everyItem(notNullValue())),
                () -> assertThat("Static + instance + field - 1", dto.enumLongHashMap.size(), equalTo(1)),
                () -> assertThat("Static + instance + field - 1", dto.enumLongHashMap.values(), everyItem(equalTo(1L))),
                () -> assertThat("Instance + field - 2", dto.doubleLocalDateTimeTreeMap.size(), equalTo(1)),
                () -> assertThat("Instance + field - 2", dto.doubleLocalDateTimeTreeMap.keySet(), everyItem(equalTo(1D))),
                () -> assertThat("Static + Instance + field - 2", dto.yearBooleanMap.size(), equalTo(20)),
                () -> assertThat("Static + Instance + field - 2", dto.yearBooleanMap.values(), everyItem(equalTo(TRUE))),
                () -> assertThat("Unknown type", dto.customDtoOfCustomDto, nullValue())
        );

    }


    @Value
    static class CustomDto {
        String argument;
    }

    @AllArgsConstructor
    @NoArgsConstructor
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
