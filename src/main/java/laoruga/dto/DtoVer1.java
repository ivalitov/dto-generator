package laoruga.dto;


import laoruga.CharSet;
import laoruga.SystemType;
import laoruga._demo.annotation_style.ArrearsBusinessRule;
import laoruga._demo.second_style.ArrearsGenerator2;
import laoruga._demo.second_style.ClosedDateGenerator;
import laoruga.markup.rules.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    @LocalDateTimeFieldRules(leftShiftDays = 365 * 3, rightShiftDays = 0)
    LocalDateTime openDate;

    @CustomGenerator(clazz = ClosedDateGenerator.class, args = {"1", "5"})
    LocalDateTime closedDate;

    @StringFieldRules(maxSymbols = 25, charset = {CharSet.ENG, CharSet.NUM})
    String fieldString;

    @LongFieldRules(minValue = 1, maxValue = 10)
    Long fieldInteger;

    @DecimalFieldRules(minValue = 0, maxValue = 100000)
    Double fieldDecimal;

    @ArrearsBusinessRule(arrearsCount = 3)
    Arrears arrearsBlock_1;

    @CustomGenerator(clazz = ArrearsGenerator2.class, args = {"5"})
    Arrears arrearsBlock_2;

    @EnumFieldRules(possibleValues = {"NBCH", "EI", "GP", "ASSD"}, className = "laoruga.SystemType")
    SystemType system;

}
