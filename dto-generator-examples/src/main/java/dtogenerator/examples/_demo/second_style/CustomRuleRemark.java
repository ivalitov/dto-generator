package dtogenerator.examples._demo.second_style;

import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;

public enum CustomRuleRemark implements ICustomRuleRemark {
    WITH_ARREARS(ArrearsGenerator2.class, 2),
    WITHOUT_ARREARS(ArrearsGenerator2.class, 0),
    CLOSED(ClosedDateGenerator.class, 0),
    OPEN(ClosedDateGenerator.class, 0);

    private final Class<? extends ICustomGenerator<?>> generatorClass;
    private final int reqArgsNumber;

    CustomRuleRemark(Class<? extends ICustomGenerator<?>> generatorClass, int reqArgsNumber) {
        this.generatorClass = generatorClass;
        this.reqArgsNumber = reqArgsNumber;
    }

    @Override
    public Class<? extends ICustomGenerator<?>> getGeneratorClass() {
        return generatorClass;
    }

    @Override
    public int requiredArgsNumber() {
        return reqArgsNumber;
    }


}
