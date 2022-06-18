package laoruga.dtogenerator.api.tests.data.dtoclient;

import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import laoruga.dtogenerator.api.tests.data.customgenerator.ClientInfoGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

@Getter
@NoArgsConstructor
public class ClientDto {

    public static final String PREFIX = "PREFIX-";

    @CustomGenerator(generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfo;

    @CustomGenerator(args = PREFIX, generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfoWithPrefix;

    @StringRule
    private String stringRequiredForClient;

    /*
     * Collections
     */

    @ListRules()
    @CustomGenerator(generatorClass = ClientInfoGenerator.class)
    private List<ClientInfoDto> clients;

    @ListRules()
    @IntegerRule
    private List<Integer> arrayListIntegerRules;

    @ListRules(minSize = 5, maxSize = 5, listClass = LinkedList.class)
    @IntegerRule(minValue = 1, maxValue = 2)
    private List<Integer> linkedListExplicitRules;

}