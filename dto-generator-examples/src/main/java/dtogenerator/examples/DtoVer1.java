package dtogenerator.examples;


import dtogenerator.api.constants.CharSet;
import dtogenerator.examples._demo.annotation_style.ArrearsBusinessRule;
import dtogenerator.examples._demo.second_style.ArrearsGenerator2;
import dtogenerator.examples._demo.second_style.ClosedDateGenerator;
import dtogenerator.api.markup.rules.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    @LocalDateTimeFieldRules(leftShiftDays = 365 * 3, rightShiftDays = 0)
    LocalDateTime openDate;

    @CustomGenerator(generatorClass = ClosedDateGenerator.class, args = {"1", "5"})
    LocalDateTime closedDate;

    @StringFieldRules(maxSymbols = 25, charset = {CharSet.ENG, CharSet.NUM})
    String fieldString;

    @LongFieldRules(minValue = 1, maxValue = 10)
    Long fieldInteger;

    @DecimalFieldRules(minValue = 0, maxValue = 100000)
    Double fieldDecimal;

    @ArrearsBusinessRule(arrearsCount = 3)
    Arrears arrearsBlock_1;

    @CustomGenerator(generatorClass = ArrearsGenerator2.class, args = {"5"})
    Arrears arrearsBlock_2;

    @EnumFieldRules(possibleEnumNames = {"NBCH", "EI", "GP", "ASSD"}, enumClass = SystemType.class)
    SystemType system;

}
