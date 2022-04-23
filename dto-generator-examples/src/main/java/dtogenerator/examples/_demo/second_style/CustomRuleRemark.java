package dtogenerator.examples._demo.second_style;

import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

public enum CustomRuleRemark implements IExtendedRuleRemark {
    WITH_ARREARS(ArrearsGenerator2.class, 2),
    WITHOUT_ARREARS(ArrearsGenerator2.class, 0),
    CLOSED(ClosedDateGenerator.class, 0),
    OPEN(ClosedDateGenerator.class, 0);

    private final Class<? extends IGenerator<?>> generatorClass;
    private final int reqArgsNumber;

    CustomRuleRemark(Class<? extends IGenerator<?>> generatorClass, int reqArgsNumber) {
        this.generatorClass = generatorClass;
        this.reqArgsNumber = reqArgsNumber;
    }

    @Override
    public Class<? extends IGenerator<?>> getGeneratorClass() {
        return generatorClass;
    }

    @Override
    public int requiredArgsNumber() {
        return reqArgsNumber;
    }


}
