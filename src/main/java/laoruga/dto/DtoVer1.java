package laoruga.dto;


import laoruga.ChField;
import laoruga.CharSet;
import laoruga.SystemType;
import laoruga.custom.ChBusinessRule;
import laoruga.markup.bounds.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    @LocalDateTimeFieldBounds(leftShiftDays = 365 * 3, rightShiftDays = 0)
    LocalDateTime openDate;

    @ChBusinessRule(ChField.CLOSED_DATE)
    LocalDateTime closedDate;

    @StringFieldBounds(maxSymbols = 25, charset = {CharSet.ENG, CharSet.NUM})
    String fieldString;

    @LongFieldBounds(minValue = 1, maxValue = 10)
    Long fieldInteger;

    @DecimalFieldBounds(minValue = 0, maxValue = 100000)
    Double fieldDecimal;

    @ChBusinessRule(ChField.ARREARS)
    Arrears arrearsBlock;

    @EnumFieldBounds(possibleValues = {"NBCH", "EI", "GP", "ASSD"}, className = "laoruga.SystemType")
    SystemType system;

}
