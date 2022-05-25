package laoruga.dtogenerator.api.tests.data.customgenerator;

import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

public enum ClientRemark implements ICustomRuleRemark {
    CLIENT_TYPE(1),
    DOCUMENT(1);

    private final int reqArgsNumber;

    ClientRemark(int reqArgsNumber) {
        this.reqArgsNumber = reqArgsNumber;
    }

    @Override
    public Class<? extends ICustomGenerator<?>> getGeneratorClass() {
        return ClientInfoGenerator.class;
    }

    @Override
    public int requiredArgsNumber() {
        return reqArgsNumber;
    }


}
