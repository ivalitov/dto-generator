package org.laoruga.dtogenerator.functional;

import org.exparity.hamcrest.date.InstantMatchers;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.exparity.hamcrest.date.LocalTimeMatchers;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoFieldShift;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;

import java.time.*;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
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
                                InstantMatchers.sameOrAfter(NOW_INSTANT.minusSeconds(60))
                        ).and(
                                InstantMatchers.sameOrBefore(NOW_INSTANT.plusSeconds(60))
                        ))
        );

    }

}
