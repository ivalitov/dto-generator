package dtogenerator.examples;


import dtogenerator.examples._demo.second_style.ArrearsGenerator2;
import dtogenerator.examples._demo.second_style.ClosedDateGenerator;
import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    @LocalDateTimeRules(leftShiftDays = 365 * 3, rightShiftDays = 0)
    LocalDateTime openDate;

    @CustomGenerator(generatorClass = ClosedDateGenerator.class, args = {"1", "5"})
    LocalDateTime closedDate;

    @StringRules(maxSymbols = 25, charset = {CharSet.ENG, CharSet.NUM}, chars = "*^()")
    String fieldString;

    @LongRules(minValue = 1, maxValue = 10)
    Long fieldInteger;

    @DoubleRules(minValue = 0, maxValue = 100000, precision = 10)
    Double fieldDecimal;

    @CustomGenerator(generatorClass = ArrearsGenerator2.class, args = {"5"})
    Arrears arrearsBlock_2;

    @EnumRules(possibleEnumNames = {"NBCH", "EI", "GP", "ASSD"}, enumClass = SystemType.class)
    SystemType system;

}
