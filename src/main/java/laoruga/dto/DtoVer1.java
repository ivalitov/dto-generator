package laoruga.dto;


import laoruga.ChField;
import laoruga.CharSet;
import laoruga.SystemType;
import laoruga.custom.ArrearsBusinessRule;
import laoruga.markup.bounds.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    @LocalDateTimeFieldBounds(leftShiftDays = 365 * 3, rightShiftDays = 0)
    LocalDateTime openDate;

    @LocalDateTimeFieldBounds(leftShiftDays = 0, rightShiftDays = 365)
    LocalDateTime closedDate;

    @StringFieldBounds(maxSymbols = 25, charset = {CharSet.ENG, CharSet.NUM})
    String fieldString;

    @LongFieldBounds(minValue = 1, maxValue = 10)
    Long fieldInteger;

    @DecimalFieldBounds(minValue = 0, maxValue = 100000)
    Double fieldDecimal;

    @ArrearsBusinessRule(arrearsCount = 3)
    Arrears arrearsBlock;

    @EnumFieldBounds(possibleValues = {"NBCH", "EI", "GP", "ASSD"}, className = "laoruga.SystemType")
    SystemType system;

}
