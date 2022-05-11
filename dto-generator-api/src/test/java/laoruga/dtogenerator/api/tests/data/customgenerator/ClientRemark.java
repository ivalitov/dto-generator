package laoruga.dtogenerator.api.tests.data.customgenerator;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;

public enum ClientRemark implements ICustomRuleRemark {
    CLIENT_TYPE(1),
    DOCUMENT(1);

    private final int reqArgsNumber;

    ClientRemark(int reqArgsNumber) {
        this.reqArgsNumber = reqArgsNumber;
    }

    @Override
    public Class<? extends IGenerator<?>> getGeneratorClass() {
        return ClientInfoGenerator.class;
    }

    @Override
    public int requiredArgsNumber() {
        return reqArgsNumber;
    }


}
