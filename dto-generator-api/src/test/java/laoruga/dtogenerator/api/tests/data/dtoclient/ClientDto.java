package laoruga.dtogenerator.api.tests.data.dtoclient;

import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import laoruga.dtogenerator.api.tests.data.customgenerator.ClientInfoGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientDto {

    @CustomGenerator(generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfo;

    @StringRules
    private String stringRequiredForClient;

}