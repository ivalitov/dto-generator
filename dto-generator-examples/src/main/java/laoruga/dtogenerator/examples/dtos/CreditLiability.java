package laoruga.dtogenerator.examples.dtos;

import java.time.LocalDateTime;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CreditLiability {

    String currency;
    Double initialAmount;
    Double amount;
    private LocalDateTime openDate;
    private LocalDateTime closedDate;
    private LocalDateTime closedDateScheduled;

}
