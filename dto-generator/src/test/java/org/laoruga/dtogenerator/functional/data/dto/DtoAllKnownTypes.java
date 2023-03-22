package org.laoruga.dtogenerator.functional.data.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.functional.data.customgenerator.CustomIntegerGenerator;
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
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class DtoAllKnownTypes {

    @StringRule
    String string;

    @NumberRule
    Integer integer;

    @NumberRule
    Long aLong;

    @DecimalRule
    Double aDouble;

    @BooleanRule Boolean aBoolean;

    @DateTimeRule
    LocalDateTime localDateTime;

    @EnumRule
    ClientType clientType;

    @CollectionRule(element = @Entry(stringRule = @StringRule))
    List<String> listOfString;

    @CollectionRule(element = @Entry(numberRule = @NumberRule))
    Set<Long> setOfLong;

    @CollectionRule(element = @Entry(enumRule = @EnumRule))
    LinkedList<ClientType> linkedListOfEnum;

    @NestedDtoRule
    DtoAllKnownTypesNested innerDto;

    @CustomRule(generatorClass = CustomIntegerGenerator.class, args = "888")
    Integer customInteger;

    Map<String, Integer> stringIntegerMap;

    public String getLocalDateTime() {
        return localDateTime.toString();
    }

    @Transient
    public LocalDateTime getLocalDateTimeAsIs() {
        return localDateTime;
    }
}
