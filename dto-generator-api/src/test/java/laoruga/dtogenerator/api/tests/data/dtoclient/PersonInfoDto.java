package laoruga.dtogenerator.api.tests.data.dtoclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonInfoDto extends ClientInfoDto {
    String firstName;
    String secondName;
    String middleName;
    Document document;
}
