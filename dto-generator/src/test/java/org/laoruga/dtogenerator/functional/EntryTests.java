package org.laoruga.dtogenerator.functional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Il'dar Valitov
 * Created on 28.04.2023
 */
public class EntryTests {

    static class DtoWrongArrayElement {

        @ArrayRule(element = @Entry(integralRule = {@IntegralRule, @IntegralRule}))
        int[] intArray;

    }

    static class DtoWrongCollectionElement {

        @CollectionRule(element = @Entry(stringRule = {@StringRule, @StringRule}))
        List<String> stringList;

    }

    static class DtoWrongMapKeyAndValue {

        @MapRule(
                key = @Entry(decimalRule = {@DecimalRule, @DecimalRule}),
                value = @Entry(dateTimeRule = {@DateTimeRule, @DateTimeRule}))
        Map<Float, LocalDateTime> floatLocalDateTimeMap;

    }

    static class DtoWrongMapKey {

        @MapRule(
                key = @Entry(decimalRule = {@DecimalRule, @DecimalRule}),
                value = @Entry(dateTimeRule = {@DateTimeRule}))
        Map<Float, LocalDateTime> floatLocalDateTimeMap;

    }

    static class DtoWrongMapValue {

        @MapRule(
                key = @Entry(decimalRule = {@DecimalRule}),
                value = @Entry(enumRule = {@EnumRule, @EnumRule}))
        Map<Float, ClientType> floatLocalDateTimeMap;

    }

    @Tag("NEGATIVE_TEST")
    @ParameterizedTest
    @ValueSource(classes = {DtoWrongArrayElement.class, DtoWrongCollectionElement.class, DtoWrongMapKeyAndValue.class, DtoWrongMapKey.class, DtoWrongMapValue.class})
    void moreThanOneElementRuleInEntry(Class<?> dtoClass) {

        DtoGeneratorException exception = assertThrows(DtoGeneratorException.class,
                () -> DtoGenerator.builder(dtoClass).build()
        );
        assertThat(ExceptionUtils.getStackTrace(exception), containsString("More than one annotation found"));
    }

}
