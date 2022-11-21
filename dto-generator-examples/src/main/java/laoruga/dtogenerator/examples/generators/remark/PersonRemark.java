package laoruga.dtogenerator.examples.generators.remark;

import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;
import laoruga.dtogenerator.examples.generators.CustomGeneratorRemarkable;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
public enum PersonRemark implements ICustomRuleRemark {

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
