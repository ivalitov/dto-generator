package org.laoruga.dtogenerator.examples.generators.custom.remark;

import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
public enum PersonRemark implements CustomRuleRemarkArgs {

    MIN_AGE(2),
    MAX_AGE(2),
    GENDER(1),
    MIN_WEIGHT(2),
    MAX_WEIGHT(2),
    MIN_GROWTH(2),
    MAX_GROWTH(2),
    ;

    private final int argsCount;

    PersonRemark(int argsCount) {
        this.argsCount = argsCount;
    }

    @Override
    public int minimumArgsNumber() {
        return argsCount;
    }
}
