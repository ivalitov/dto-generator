package org.laoruga.dtogenerator.examples.generators.custom.remark;

import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGeneratorRemarkable;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
public enum PersonRemark implements CustomRuleRemarkArgs {

    AGE_RANGE(2),
    GENDER(1),
    WEIGHT_RANGE(2),
    GROWTH_RANGE(2),
    ;

    private final int argsCount;

    PersonRemark(int argsCount) {
        this.argsCount = argsCount;
    }

    @Override
    public Class<? extends CustomGenerator<?>> getGeneratorClass() {
        return CustomGeneratorRemarkable.class;
    }

    @Override
    public int requiredArgsNumber() {
        return argsCount;
    }
}
