package laoruga.dtogenerator.api.tests.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class OrgInfoDto extends ClientInfoDto {
    String orgName;
}
