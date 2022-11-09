package dtogenerator.examples._demo.second_style;

import dtogenerator.examples.Arrears;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Arrays;
import java.util.List;

import static dtogenerator.examples._demo.second_style.CustomRuleRemark.WITHOUT_ARREARS;
import static dtogenerator.examples._demo.second_style.CustomRuleRemark.WITH_ARREARS;

@NoArgsConstructor
public class ArrearsGenerator2 implements
        ICustomGeneratorArgs<Arrears>,
        ICustomGeneratorRemarkable<Arrears> {

    int arrearsCount;
    ICustomRuleRemark[] ruleRemarks;

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
    public ArrearsGenerator2 setArgs(String[] args) {
        if (args.length == 1) {
            arrearsCount = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            arrearsCount = new RandomDataGenerator().nextInt(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } else {
            throw new IllegalArgumentException("2 args expected, but was: " + Arrays.asList(args));
        }
        return this;
    }

    @Override
    public void setRuleRemarks(List<CustomRuleRemarkWrapper> ruleRemarks) {
        for (CustomRuleRemarkWrapper ruleRemark : ruleRemarks) {
            ICustomRuleRemark enumType = ruleRemark.getWrappedRuleRemark();
            if (enumType == WITH_ARREARS) {
                arrearsCount = Integer.parseInt(ruleRemark.getArgsList().get(0));
            } else if (enumType == WITHOUT_ARREARS) {
                arrearsCount = 0;
            }
        }
    }
}
