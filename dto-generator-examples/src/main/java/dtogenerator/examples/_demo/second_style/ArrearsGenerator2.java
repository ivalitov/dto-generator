package dtogenerator.examples._demo.second_style;

import dtogenerator.examples.Arrears;
import dtogenerator.api.markup.ICustomGenerator;
import dtogenerator.api.markup.IExtendedRuleRemark;
import dtogenerator.api.markup.IRemarkableCustomGenerator;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Arrays;

import static dtogenerator.examples._demo.second_style.CustomRuleRemark.WITHOUT_ARREARS;
import static dtogenerator.examples._demo.second_style.CustomRuleRemark.WITH_ARREARS;

@NoArgsConstructor
public class ArrearsGenerator2 implements
        ICustomGenerator<Arrears>,
        IRemarkableCustomGenerator<Arrears> {

    int arrearsCount;
    IExtendedRuleRemark[] ruleRemarks;

    @Override
    public Arrears generate() {
        Arrears arrears = null;
        if (ruleRemarks == null) {
            ruleRemarks = new IExtendedRuleRemark[1];
            ruleRemarks[0] = new RandomDataGenerator().nextInt(0, 1) == 0 ?
                    WITH_ARREARS : WITHOUT_ARREARS;
        }
        for (IExtendedRuleRemark ruleRemark : ruleRemarks) {
           if (ruleRemark == WITH_ARREARS) {
               arrears = new Arrears();
               for (int i = 1; i <= arrearsCount; i++) {
                   arrears.addArrear(i);
               }
           } else if (ruleRemark == WITHOUT_ARREARS) {
               arrears = null;
           } else {
               throw new RuntimeException("Unexpected rule remark " + ruleRemark);
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
        this.ruleRemarks = ruleRemarks;
    }
}
