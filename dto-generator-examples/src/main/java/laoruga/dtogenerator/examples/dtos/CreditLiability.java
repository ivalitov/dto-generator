package laoruga.dtogenerator.examples.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
@Builder
public class CreditLiability {

    String currency;
    Double initialAmount;
    Double currentAmount;
    private LocalDateTime openDate;
    private LocalDateTime closedDate;
    private LocalDateTime closedDateScheduled;

}
