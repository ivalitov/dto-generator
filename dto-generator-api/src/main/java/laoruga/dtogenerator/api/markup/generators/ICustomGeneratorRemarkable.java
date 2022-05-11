package laoruga.dtogenerator.api.markup.generators;

import com.sun.istack.internal.Nullable;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;

import java.util.List;

public interface ICustomGeneratorRemarkable<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(List<CustomRuleRemarkWrapper> iRuleRemarks);

    @Nullable
    static CustomRuleRemarkWrapper getRemarkOrNull(ICustomRuleRemark remark, List<CustomRuleRemarkWrapper> ruleRemarks) {
        for (CustomRuleRemarkWrapper ruleRemark : ruleRemarks) {
            if (ruleRemark.getWrappedRuleRemark().equals(remark)) {
                return ruleRemark;
            }
        }
        return null;
    }

}
