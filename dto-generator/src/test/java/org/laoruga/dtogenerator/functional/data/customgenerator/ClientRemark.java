package org.laoruga.dtogenerator.functional.data.customgenerator;

import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

public enum ClientRemark implements CustomRuleRemarkArgs {
    CLIENT_TYPE(1),
    DOCUMENT(1);

    private final int reqArgsNumber;

    ClientRemark(int reqArgsNumber) {
        this.reqArgsNumber = reqArgsNumber;
    }

    @Override
    public Class<? extends CustomGenerator<?>> getGeneratorClass() {
        return ClientInfoGenerator.class;
    }

    @Override
    public int requiredArgsNumber() {
        return reqArgsNumber;
    }


}
