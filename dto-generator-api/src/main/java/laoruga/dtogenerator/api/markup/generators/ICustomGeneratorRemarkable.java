package laoruga.dtogenerator.api.markup.generators;

import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;

import java.util.List;

public interface ICustomGeneratorRemarkable<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(List<CustomRuleRemarkWrapper> iRuleRemarks);

    static CustomRuleRemarkWrapper findWrappedRemarkOrReturnNull(ICustomRuleRemark remark, List<CustomRuleRemarkWrapper> wrappedRuleRemarks) {
        if (wrappedRuleRemarks == null) {
            return null;
        }
        for (CustomRuleRemarkWrapper ruleRemark : wrappedRuleRemarks) {
            if (ruleRemark.getWrappedRuleRemark().equals(remark)) {
                return ruleRemark;
            }
        }
        return null;
    }

}
