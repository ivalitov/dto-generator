package org.laoruga.dtogenerator.functional.data.dtoclient;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

@Data
@AllArgsConstructor
public class Document {
    DocType type;
    String series;
    String number;
    LocalDate issueDate;
    LocalDate validityDate;
}
