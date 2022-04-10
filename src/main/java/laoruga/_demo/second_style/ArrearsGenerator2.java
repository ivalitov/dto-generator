package laoruga._demo.second_style;

import laoruga.dto.Arrears;
import laoruga.markup.ICustomGenerator;
import laoruga.markup.IExtendedRuleRemark;
import laoruga.markup.IRemarkableCustomGenerator;
import lombok.NoArgsConstructor;

import static laoruga._demo.second_style.CustomRuleRemark.WITHOUT_ARREARS;
import static laoruga._demo.second_style.CustomRuleRemark.WITH_ARREARS;

@NoArgsConstructor
public class ArrearsGenerator2 implements
        ICustomGenerator<Arrears>,
        IRemarkableCustomGenerator<Arrears> {

    int arrearsCount;
    IExtendedRuleRemark[] ruleRemarks;

    @Override
    public Arrears generate() {
        Arrears arrears = null;
        for (IExtendedRuleRemark ruleRemark : ruleRemarks) {
           if (ruleRemark == WITH_ARREARS) {
               arrears = new Arrears();
               for (int i = 0; i < arrearsCount; i++) {
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
        arrearsCount = Integer.parseInt(args[0]);
    }


    @Override
    public void setRuleRemarks(IExtendedRuleRemark... ruleRemarks) {
        this.ruleRemarks = ruleRemarks;
    }
}
