package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.exparity.hamcrest.date.InstantMatchers;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.exparity.hamcrest.date.LocalTimeMatchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoFieldShift;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.generator.config.dto.datetime.ChronoFieldConfig;
import org.laoruga.dtogenerator.generator.config.dto.datetime.ChronoUnitConfig;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.time.*;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.Bounds.CHRONO_UNIT_SHIFT_LEFT;
import static org.laoruga.dtogenerator.constants.Bounds.CHRONO_UNIT_SHIFT_RIGHT;
import static org.laoruga.dtogenerator.constants.RuleRemark.*;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("DATE_TIME_RULES")
public class DateTimeTests {

    static class Dto {

        @DateTimeRule(chronoUnitShift = @ChronoUnitShift(unit = DAYS))
        LocalDateTime localDateTime;

        @DateTimeRule(chronoFieldShift = @ChronoFieldShift(unit = ChronoField.DAY_OF_WEEK, shift = 3))
        LocalDate localDate;

        @DateTimeRule
        LocalTime localTime;

        @DateTimeRule
        Year year;

        @DateTimeRule
        YearMonth yearMonth;

        @DateTimeRule
        Instant instant;
    }

    @Test
    void annotationConfig() {

        final Instant NOW_INSTANT = Instant.now();
        final LocalDateTime NOW = LocalDateTime.now();

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.localDateTime,
                        both(
                                LocalDateTimeMatchers.sameOrAfter(NOW.plus(CHRONO_UNIT_SHIFT_LEFT, DAYS))
                        ).and(
                                LocalDateTimeMatchers.sameOrBefore(NOW.plus(CHRONO_UNIT_SHIFT_RIGHT, DAYS))
                        )),
                () -> assertThat(dto.localDate,
                        equalTo(
                                NOW.toLocalDate().with(ChronoField.DAY_OF_WEEK, 3)
                        )),
                () -> assertThat(dto.localTime,
                        both(
                                LocalTimeMatchers.sameOrAfter(NOW.toLocalTime().minusMinutes(1))
                        ).and(
                                LocalTimeMatchers.sameOrBefore(NOW.toLocalTime().plusMinutes(1))
                        )),
                () -> assertThat(dto.year.getValue(),
                        equalTo(
                                NOW.getYear()
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(NOW.getYear(), NOW.getMonth())
                        )),
                () -> assertThat(dto.instant,
                        both(
                                InstantMatchers.sameOrAfter(NOW_INSTANT.minusSeconds(1))
                        ).and(
                                InstantMatchers.sameOrBefore(NOW_INSTANT.plusSeconds(5))
                        ))
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    public void staticConfig() {

        final LocalDateTime NOW = LocalDateTime.now();
        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        staticConfig.getDateTimeConfig(LocalDateTime.class)
                .addChronoConfig(ChronoUnitConfig.newAbsolute(1, DAYS));

        staticConfig.getDateTimeConfig(LocalDate.class)
                .addChronoConfig(ChronoFieldConfig.newAbsolute(2, ChronoField.DAY_OF_MONTH));

        staticConfig.getDateTimeConfig(LocalTime.class)
                .addChronoConfig(ChronoUnitConfig.newAbsolute(3, HOURS));

        staticConfig.getDateTimeConfig(Year.class)
                .addChronoConfig(ChronoFieldConfig.newAbsolute(2004, ChronoField.YEAR));

        staticConfig.getDateTimeConfig(YearMonth.class)
                .addChronoConfig(ChronoUnitConfig.newBounds(5, 5, MONTHS));

        staticConfig.getDateTimeConfig(Instant.class)
                .addChronoConfig(ChronoFieldConfig.newBounds(-100, 6, ChronoField.INSTANT_SECONDS))
                .setRuleRemark(MAX_VALUE);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.localDateTime.toLocalDate(),
                        equalTo(
                                NOW.toLocalDate().plusDays(1)
                        )),
                () -> assertThat(dto.localDate,
                        equalTo(
                                NOW.toLocalDate().with(ChronoField.DAY_OF_MONTH, 2)
                        )),
                () -> assertThat(dto.localTime,
                        both(
                                LocalTimeMatchers.sameOrAfter(NOW.toLocalTime().plusHours(3).minusMinutes(1))
                        ).and(
                                LocalTimeMatchers.sameOrBefore(NOW.toLocalTime().plusHours(3).plusMinutes(1))
                        )),
                () -> assertThat(dto.year.getValue(),
                        equalTo(
                                2004
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(NOW.getYear(), NOW.getMonth().plus(5))
                        )),
                () -> assertThat(dto.instant.getEpochSecond(),
                        equalTo(6L))
        );
    }

    @Test
    void instanceConfig() {

        final LocalDateTime NOW = LocalDateTime.now();

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();

        instanceConfig.getDateTimeConfig(LocalDateTime.class)
                .addChronoConfig(ChronoUnitConfig.newAbsolute(1, DAYS));

        instanceConfig.getDateTimeConfig(LocalDate.class)
                .addChronoConfig(ChronoFieldConfig.newAbsolute(2, ChronoField.DAY_OF_MONTH));

        instanceConfig.getDateTimeConfig(LocalTime.class)
                .addChronoConfig(ChronoUnitConfig.newAbsolute(3, HOURS));

        instanceConfig.getDateTimeConfig(Year.class)
                .addChronoConfig(ChronoFieldConfig.newAbsolute(2004, ChronoField.YEAR));

        instanceConfig.getDateTimeConfig(YearMonth.class)
                .addChronoConfig(ChronoUnitConfig.newBounds(5, 100, MONTHS))
                .setRuleRemark(MIN_VALUE);

        instanceConfig.getDateTimeConfig(Instant.class)
                .addChronoConfig(ChronoFieldConfig.newBounds(6, 6, ChronoField.INSTANT_SECONDS));

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.localDateTime.toLocalDate(),
                        equalTo(
                                NOW.toLocalDate().plusDays(1)
                        )),
                () -> assertThat(dto.localDate,
                        equalTo(
                                NOW.toLocalDate().with(ChronoField.DAY_OF_MONTH, 2)
                        )),
                () -> assertThat(dto.localTime,
                        both(
                                LocalTimeMatchers.sameOrAfter(NOW.toLocalTime().plusHours(3).minusMinutes(1))
                        ).and(
                                LocalTimeMatchers.sameOrBefore(NOW.toLocalTime().plusHours(3).plusMinutes(1))
                        )),
                () -> assertThat(dto.year.getValue(),
                        equalTo(
                                2004
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(NOW.getYear(), NOW.getMonth().plus(5))
                        )),
                () -> assertThat(dto.instant.getEpochSecond(),
                        equalTo(6L))
        );
    }

    @Test
    void fieldConfig() {

        final LocalDateTime NOW = LocalDateTime.now();

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder
                .setTypeGeneratorConfig("localDateTime", DateTimeConfig.builder()
                        .addChronoConfig(ChronoUnitConfig.newAbsolute(1, DAYS)).build())

                .setTypeGeneratorConfig("localDate", DateTimeConfig.builder()
                        .addChronoConfig(ChronoFieldConfig.newAbsolute(2, ChronoField.DAY_OF_MONTH)).build())

                .setTypeGeneratorConfig("localTime", DateTimeConfig.builder()
                        .addChronoConfig(ChronoUnitConfig.newAbsolute(3, HOURS)).build())

                .setTypeGeneratorConfig("year", DateTimeConfig.builder()
                        .addChronoConfig(ChronoFieldConfig.newAbsolute(2004, ChronoField.YEAR)).build())

                .setTypeGeneratorConfig("yearMonth", DateTimeConfig.builder()
                        .addChronoConfig(ChronoUnitConfig.newBounds(5, 5, MONTHS)).build())

                .setTypeGeneratorConfig("instant", DateTimeConfig.builder()
                        .addChronoConfig(ChronoFieldConfig.newBounds(6, 6, ChronoField.INSTANT_SECONDS)).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.localDateTime.toLocalDate(),
                        equalTo(
                                NOW.toLocalDate().plusDays(1)
                        )),
                () -> assertThat(dto.localDate,
                        equalTo(
                                NOW.toLocalDate().with(ChronoField.DAY_OF_MONTH, 2)
                        )),
                () -> assertThat(dto.localTime,
                        both(
                                LocalTimeMatchers.sameOrAfter(NOW.toLocalTime().plusHours(3).minusMinutes(1))
                        ).and(
                                LocalTimeMatchers.sameOrBefore(NOW.toLocalTime().plusHours(3).plusMinutes(1))
                        )),
                () -> assertThat(dto.year.getValue(),
                        equalTo(
                                2004
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(NOW.getYear(), NOW.getMonth().plus(5))
                        )),
                () -> assertThat(dto.instant.getEpochSecond(),
                        equalTo(6L)));
    }

    @Test
    void overrideGeneratorByField() {

        final LocalDateTime NOW = LocalDateTime.now();
        final Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator("localDateTime", () -> NOW.plusDays(1));
        builder.setGenerator("localDate", () -> NOW.toLocalDate().plusDays(2));
        builder.setGenerator("localTime", () -> NOW.toLocalTime().plusHours(3));
        builder.setGenerator("year", () -> Year.of(NOW.getYear() + 4));
        builder.setGenerator("yearMonth", () -> YearMonth.of(NOW.getYear(), 5));
        builder.setGenerator("instant", () -> NOW_INSTANT);

        Dto dto = builder.build().generateDto();

        commonAssert(dto, NOW);
    }

    @Test
    void overrideGeneratorByType() {

        final LocalDateTime NOW = LocalDateTime.now();
        final Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(LocalDateTime.class, () -> NOW.plusDays(1));
        builder.setGenerator(LocalDate.class, () -> NOW.toLocalDate().plusDays(2));
        builder.setGenerator(LocalTime.class, () -> NOW.toLocalTime().plusHours(3));
        builder.setGenerator(Year.class, () -> Year.of(NOW.getYear() + 4));
        builder.setGenerator(YearMonth.class, () -> YearMonth.of(NOW.getYear(), 5));
        builder.setGenerator(Instant.class, () -> NOW_INSTANT);

        Dto dto = builder.build().generateDto();

        commonAssert(dto, NOW);
    }

    @Test
    void overrideGeneratorByTypeAndField() {

        final LocalDateTime NOW = LocalDateTime.now();
        final Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator("localDateTime", () -> NOW.plusDays(1));
        builder.setGenerator("localDate", () -> NOW.toLocalDate().plusDays(2));
        builder.setGenerator("localTime", () -> NOW.toLocalTime().plusHours(3));
        builder.setGenerator("year", () -> Year.of(NOW.getYear() + 4));
        builder.setGenerator("yearMonth", () -> YearMonth.of(NOW.getYear(), 5));
        builder.setGenerator("instant", () -> NOW_INSTANT);

        builder.setGenerator(LocalDateTime.class, () -> NOW.plusDays(11));
        builder.setGenerator(LocalDate.class, () -> NOW.toLocalDate().plusDays(22));
        builder.setGenerator(LocalTime.class, () -> NOW.toLocalTime().plusHours(33));
        builder.setGenerator(Year.class, () -> Year.of(NOW.getYear() + 44));
        builder.setGenerator(YearMonth.class, () -> YearMonth.of(NOW.getYear(), 55));
        builder.setGenerator(Instant.class, () -> NOW_INSTANT.plusSeconds(66));

        Dto dto = builder.build().generateDto();

        commonAssert(dto, NOW);
    }

    private static void commonAssert(Dto dto, final LocalDateTime now) {
        final Instant NOW_INSTANT = now.atZone(ZoneId.systemDefault()).toInstant();

        assertAll(
                () -> assertThat(dto.localDateTime,
                        equalTo(
                                now.plusDays(1)
                        )),
                () -> assertThat(dto.localDate,
                        equalTo(
                                now.toLocalDate().plusDays(2)
                        )),
                () -> assertThat(dto.localTime,
                        equalTo(
                                now.toLocalTime().plusHours(3)
                        )),
                () -> assertThat(dto.year,
                        equalTo(
                                Year.of(now.getYear() + 4)
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(now.getYear(), 5)
                        )),
                () -> assertThat(dto.instant,
                        equalTo(NOW_INSTANT)));
    }

    static class Dto_2 {
        LocalDateTime localDateTime;
        LocalDate localDate;
        LocalTime localTime;
        Year year;
        YearMonth yearMonth;
        Instant instant;
    }


    @Test
    void withoutAnnotations() {

        final LocalDateTime NOW = LocalDateTime.now();
        final Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.localDateTime.toLocalDate(),
                        equalTo(LocalDate.now())
                ),
                () -> assertThat(dto.localDate,
                        equalTo(LocalDate.now())
                ),
                () -> assertThat(dto.localTime,
                        both(
                                LocalTimeMatchers.sameOrAfter(NOW.toLocalTime().minusMinutes(1))
                        ).and(
                                LocalTimeMatchers.sameOrBefore(NOW.toLocalTime().plusMinutes(1))
                        )),
                () -> assertThat(dto.year.getValue(),
                        equalTo(
                                NOW.getYear()
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(NOW.getYear(), NOW.getMonth())
                        )),
                () -> assertThat(dto.instant,
                        both(
                                InstantMatchers.sameOrAfter(NOW_INSTANT.minusSeconds(1))
                        ).and(
                                InstantMatchers.sameOrBefore(NOW_INSTANT.plusSeconds(5))
                        ))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    public void withoutAnnotationsWithOverriddenConfig() {

        final LocalDateTime NOW = LocalDateTime.now();
        final Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        // static
        staticConfig.getDateTimeConfig(LocalDateTime.class)
                .setRuleRemark(MAX_VALUE);
        staticConfig.getDateTimeConfig(LocalTime.class)
                .addChronoConfig(ChronoUnitConfig.newBounds(-5, 5, HOURS))
                .setRuleRemark(RANDOM_VALUE);
        staticConfig.getDateTimeConfig(Year.class)
                // obviously incorrect param
                .addChronoConfig(ChronoFieldConfig.newAbsolute(5, ChronoField.SECOND_OF_DAY));


        // instance
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();

        instanceConfig.getDateTimeConfig(LocalDateTime.class)
                .addChronoConfig(ChronoUnitConfig.newBounds(-100, 100, DAYS));
        builder.setTypeGeneratorConfig(
                LocalDate.class,
                DateTimeConfig.builder().addChronoConfig(ChronoUnitConfig.newBounds(-200, 200, DAYS)).build()
        );
        instanceConfig.getDateTimeConfig(LocalTime.class)
                .setRuleRemark(MIN_VALUE);
        instanceConfig.getDateTimeConfig(Year.class)
                // obviously incorrect param
                .addChronoConfig(ChronoUnitConfig.newAbsolute(5, SECONDS));


        // field
        builder.setTypeGeneratorConfig("localDate", DateTimeConfig.builder().ruleRemark(MIN_VALUE).build());
        builder.setTypeGeneratorConfig("localTime", DateTimeConfig.builder().ruleRemark(MAX_VALUE).build());
        builder.setTypeGeneratorConfig("year", DateTimeConfig.builder().addChronoConfig(
                ChronoUnitConfig.newAbsolute(3, YEARS)).build()
        );


        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Static + instance config", dto.localDateTime.toLocalDate(),
                        equalTo(LocalDate.now().plusDays(100))
                ),
                () -> assertThat("Type config + field", dto.localDate,
                        equalTo(LocalDate.now().minusDays(200))
                ),
                () -> assertThat("Override rule remark", dto.localTime,
                        both(
                                LocalTimeMatchers.sameOrAfter(NOW.toLocalTime().plusHours(5).minusMinutes(1))
                        ).and(
                                LocalTimeMatchers.sameOrBefore(NOW.toLocalTime().plusHours(5).plusMinutes(1))
                        )),
                () -> assertThat("Override chrono", dto.year.getValue(),
                        equalTo(
                                NOW.plusYears(3).getYear()
                        )),
                () -> assertThat(dto.yearMonth,
                        equalTo(
                                YearMonth.of(NOW.getYear(), NOW.getMonth())
                        )),
                () -> assertThat(dto.instant,
                        both(
                                InstantMatchers.sameOrAfter(NOW_INSTANT.minusSeconds(1))
                        ).and(
                                InstantMatchers.sameOrBefore(NOW_INSTANT.plusSeconds(5))
                        ))
        );

    }

}
