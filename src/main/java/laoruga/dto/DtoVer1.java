package laoruga.dto;


import laoruga.CharSet;
import laoruga.SystemType;
import laoruga.custom.ArrearsBusinessRule;
import laoruga.markup.rules.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    @LocalDateTimeFieldRules(leftShiftDays = 365 * 3, rightShiftDays = 0)
    LocalDateTime openDate;

    @CustomGenerator(className = "laoruga.ClosedDateGenerator", args = {"1", "5"})
    LocalDateTime closedDate;

    @StringFieldRules(maxSymbols = 25, charset = {CharSet.ENG, CharSet.NUM})
    String fieldString;

    @LongFieldRules(minValue = 1, maxValue = 10)
    Long fieldInteger;

    @DecimalFieldRules(minValue = 0, maxValue = 100000)
    Double fieldDecimal;

    @ArrearsBusinessRule(arrearsCount = 3)
    Arrears arrearsBlock;

    @EnumFieldRules(possibleValues = {"NBCH", "EI", "GP", "ASSD"}, className = "laoruga.SystemType")
    SystemType system;

}
