package dtogenerator.examples._demo.second_style;

import dtogenerator.examples.Arrears;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapperWithArgs;
import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Arrays;

import static dtogenerator.examples._demo.second_style.CustomRuleRemark.WITHOUT_ARREARS;
import static dtogenerator.examples._demo.second_style.CustomRuleRemark.WITH_ARREARS;

@NoArgsConstructor
public class ArrearsGenerator2 implements
        ICustomGeneratorArgs<Arrears>,
        ICustomGeneratorRemarkable<Arrears> {

    int arrearsCount;
    IExtendedRuleRemark[] ruleRemarks;

    @Override
    public Arrears generate() {
        Arrears arrears;
        if (arrearsCount == 0) {
            arrears = null;
        } else {
            arrears = new Arrears();
            for (int i = 1; i <= arrearsCount; i++) {
                arrears.addArrear(i);
            }
        }
        return arrears;
    }

    @Override
    public void setArgs(String[] args) {
        if (args.length == 1) {
            arrearsCount = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            arrearsCount = new RandomDataGenerator().nextInt(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } else {
            throw new IllegalArgumentException("2 args expected, but was: " + Arrays.asList(args));
        }
    }

    @Override
    public void setRuleRemarks(IExtendedRuleRemark... ruleRemarks) {
        for (IExtendedRuleRemark ruleRemark : ruleRemarks) {
            IExtendedRuleRemark enumType;
            if (ruleRemark instanceof ExtendedRuleRemarkWrapperWithArgs) {
                enumType = ((ExtendedRuleRemarkWrapperWithArgs) ruleRemark).getWrappedRuleRemark();
            } else {
                enumType = ruleRemark;
            }
            if (enumType == WITH_ARREARS) {
                arrearsCount = Integer.parseInt(enumType.getArgsList().get(0));
            } else if (ruleRemark == WITHOUT_ARREARS) {
                arrearsCount = 0;
            }
        }
    }
}
