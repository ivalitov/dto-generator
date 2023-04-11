package org.laoruga.dtogenerator.functional.data.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.functional.data.generator.CustomIntegerGenerator;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class DtoAllKnownTypesNested {

    @StringRule
    String string;

    @NumberRule
    Integer integer;

    @NumberRule
    Long aLong;

    @DecimalRule
    Double aDouble;

    @DateTimeRule
    LocalDateTime localDateTime;

    @EnumRule
    ClientType clientType;

    @CollectionRule
    List<Double> listOfDouble;

    @CollectionRule
    Set<Integer> setOfInteger;

    @CollectionRule
    LinkedList<ClientType> linkedListOfEnum;

    Map<String, Integer> stringIntegerMap;

    @CustomRule(generatorClass = CustomIntegerGenerator.class, args = "999")
    Integer customInteger;

    public String getLocalDateTime() {
        return localDateTime.toString();
    }

    @Transient
    public LocalDateTime getLocalDateTimeAsIs() {
        return localDateTime;
    }

}
