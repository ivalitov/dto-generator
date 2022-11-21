package org.laoruga.dtogenerator.functional.data.dtoclient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.functional.data.customgenerator.ClientInfoGenerator;

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

    @CustomRule(generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfo;

    @CustomRule(args = PREFIX, generatorClass = ClientInfoGenerator.class)
    private ClientInfoDto clientInfoWithPrefix;

    @StringRule
    private String stringRequiredForClient;

    /*
     * Collections
     */

    @ListRule()
    @CustomRule(generatorClass = ClientInfoGenerator.class)
    private List<ClientInfoDto> clients;

    @ListRule()
    @IntegerRule
    private List<Integer> arrayListIntegerRules;

    @ListRule(minSize = 5, maxSize = 5, listClass = LinkedList.class)
    @IntegerRule(minValue = 1, maxValue = 2)
    private List<Integer> linkedListExplicitRules;

}