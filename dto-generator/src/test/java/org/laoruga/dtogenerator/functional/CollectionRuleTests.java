package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;

import java.time.Year;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("DATE_TIME_RULES")
public class CollectionRuleTests {

    enum Planets {EARTH, SATURN, PLUTO, URANUS, MARS}

    static class Dto {

        @CollectionRule @StringRule
        List<String> listOfString;

        @CollectionRule @NumberRule
        Set<Integer> setOfInteger;

        @CollectionRule @DateTimeRule
        Queue<Year> queueOfYear;

        @CollectionRule @DecimalRule
        LinkedList<Double> linkedListOfDouble;

        @CollectionRule @NumberRule
        LinkedHashSet<AtomicInteger> linkedHashSetOfAtomicInteger;

        @CollectionRule @EnumRule
        ArrayDeque<Planets> arrayDequeOfEnum;

    }

    @Test
    public void annotationConfig() {

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.listOfString, not(empty())),
                () -> assertThat(dto.setOfInteger, not(empty())),
                () -> assertThat(dto.queueOfYear, not(empty())),
                () -> assertThat(dto.linkedListOfDouble, not(empty())),
                () -> assertThat(dto.linkedHashSetOfAtomicInteger, not(empty())),
                () -> assertThat(dto.arrayDequeOfEnum, not(empty()))
        );

    }

}
