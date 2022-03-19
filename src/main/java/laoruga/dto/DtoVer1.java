package laoruga.dto;


import laoruga.ChField;
import laoruga.CharSet;
import laoruga.markup.bounds.DecimalFieldBounds;
import laoruga.markup.bounds.EnumFieldBounds;
import laoruga.markup.bounds.IntFieldBounds;
import laoruga.markup.bounds.StringFieldBounds;
import laoruga.markup.FieldBusinessType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoVer1 {

    LocalDateTime openDate;

    @FieldBusinessType(ChField.CLOSED_DATE)
    LocalDateTime closedDate;

    @StringFieldBounds(maxSymbols = 25, charset = CharSet.ENG_NUM)
    String fieldString;

    @IntFieldBounds(minSymbols = 1, maxSymbols = 10)
    Integer fieldInteger;

    @DecimalFieldBounds(minSymbols = 0, maxSymbols = 100000)
    Double fieldDecimal;

    @FieldBusinessType(ChField.ARREARS)
    Arrears arrearsBlock;

    @EnumFieldBounds(possibleValues = {"NBCH", "EI", "GP", "ASSD"})
    System system;

    class Arrears {

    }

}
