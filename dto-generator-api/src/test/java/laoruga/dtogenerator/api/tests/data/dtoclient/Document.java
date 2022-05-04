package laoruga.dtogenerator.api.tests.data.dtoclient;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Document {
    DocType type;
    String series;
    String number;
    LocalDate issueDate;
    LocalDate validityDate;
}
