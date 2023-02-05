package org.laoruga.dtogenerator.examples.generators.custom.remark;

import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemarkArgs;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGeneratorRemarkable;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
public enum PersonRemark implements ICustomRuleRemarkArgs {

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
    public Class<? extends ICustomGenerator<?>> getGeneratorClass() {
        return CustomGeneratorRemarkable.class;
    }

    @Override
    public int requiredArgsNumber() {
        return argsCount;
    }
}
