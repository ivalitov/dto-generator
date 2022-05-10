package laoruga.dtogenerator.api.tests.data.dtoclient;

import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import laoruga.dtogenerator.api.tests.data.customgenerator.ClientInfoGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientDto {

    public static final String PREFIX = "PREFIX-";

    @CustomGenerator(generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfo;

    @CustomGenerator(args = PREFIX, generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfoWithPrefix;

    @StringRules
    private String stringRequiredForClient;

}