package laoruga.dtogenerator.api.tests.data.dtoclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonInfoDto extends ClientInfoDto {
    String firstName;
    String secondName;
    String middleName;
    Document document;
}
