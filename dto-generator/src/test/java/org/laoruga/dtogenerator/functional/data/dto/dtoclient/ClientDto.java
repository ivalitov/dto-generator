package org.laoruga.dtogenerator.functional.data.dto.dtoclient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.NumberRule;
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

    @CollectionRule()
    @CustomRule(generatorClass = ClientInfoGenerator.class)
    private List<ClientInfoDto> clients;

    @CollectionRule()
    @NumberRule
    private List<Integer> arrayListIntegerRules;

    @CollectionRule(minSize = 5, maxSize = 5, collectionClass = LinkedList.class)
    @NumberRule(minInt = 1, maxInt = 2)
    private List<Integer> linkedListExplicitRules;

}