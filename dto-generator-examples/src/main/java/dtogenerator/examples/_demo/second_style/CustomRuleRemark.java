package dtogenerator.examples._demo.second_style;

import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

public enum CustomRuleRemark implements IExtendedRuleRemark {
    WITH_ARREARS(ArrearsGenerator2.class),
    WITHOUT_ARREARS(ArrearsGenerator2.class),
    CLOSED(ClosedDateGenerator.class),
    OPEN(ClosedDateGenerator.class);

    private final Class<? extends IGenerator<?>> generatorClass;

    CustomRuleRemark(Class<? extends IGenerator<?>> generatorClass) {
        this.generatorClass = generatorClass;
    }

    @Override
    public Class<? extends IGenerator<?>> getGeneratorClass() {
        return generatorClass;
    }





}
