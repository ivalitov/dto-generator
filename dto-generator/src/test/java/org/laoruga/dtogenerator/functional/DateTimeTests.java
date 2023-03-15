package org.laoruga.dtogenerator.functional;

import org.exparity.hamcrest.date.InstantMatchers;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.exparity.hamcrest.date.LocalTimeMatchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoFieldShift;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.generator.configs.datetime.ChronoFieldConfig;
import org.laoruga.dtogenerator.generator.configs.datetime.ChronoUnitConfig;

import java.time.*;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.RulesInstance.CHRONO_UNIT_SHIFT;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
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
    public void annotationConfig() {

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        final LocalDateTime NOW = LocalDateTime.now();
        final Instant NOW_INSTANT = NOW.toInstant(ZoneId.systemDefault().getRules().getOffset(NOW));

        assertAll(
                () -> assertThat(dto.localDateTime,
                        both(
                                LocalDateTimeMatchers.sameOrAfter(NOW.plus(CHRONO_UNIT_SHIFT.leftBound(), DAYS))
                        ).and(
                                LocalDateTimeMatchers.sameOrBefore(NOW.plus(CHRONO_UNIT_SHIFT.rightBound(), DAYS))
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
                                InstantMatchers.sameOrBefore(NOW_INSTANT.plusSeconds(1))
                        ))
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    public void staticConfig() {

        final LocalDateTime NOW = LocalDateTime.now();

        TypeGeneratorsConfigSupplier staticConfig = DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig();

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
                .addChronoConfig(ChronoFieldConfig.newBounds(6, 6, ChronoField.INSTANT_SECONDS));

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

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
    public void instanceConfig() {

        final LocalDateTime NOW = LocalDateTime.now();

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier instanceConfig = builder.getTypeGeneratorConfig();

        instanceConfig.getDateTimeConfig(LocalDateTime.class)
                .addChronoConfig(ChronoUnitConfig.newAbsolute(1, DAYS));

        instanceConfig.getDateTimeConfig(LocalDate.class)
                .addChronoConfig(ChronoFieldConfig.newAbsolute(2, ChronoField.DAY_OF_MONTH));

        instanceConfig.getDateTimeConfig(LocalTime.class)
                .addChronoConfig(ChronoUnitConfig.newAbsolute(3, HOURS));

        instanceConfig.getDateTimeConfig(Year.class)
                .addChronoConfig(ChronoFieldConfig.newAbsolute(2004, ChronoField.YEAR));

        instanceConfig.getDateTimeConfig(YearMonth.class)
                .addChronoConfig(ChronoUnitConfig.newBounds(5, 5, MONTHS));

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

}
