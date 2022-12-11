package org.laoruga.dtogenerator.functional.data.dto.dtoclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class OrgInfoDto extends ClientInfoDto {
    String orgName;
}
