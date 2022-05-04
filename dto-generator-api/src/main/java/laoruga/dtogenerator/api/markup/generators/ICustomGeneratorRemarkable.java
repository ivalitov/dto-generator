package laoruga.dtogenerator.api.markup.generators;

import com.sun.istack.internal.Nullable;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;

public interface ICustomGeneratorRemarkable<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(ExtendedRuleRemarkWrapper... iRuleRemarks);

    @Nullable
    static ExtendedRuleRemarkWrapper getRemarkOrNull(IExtendedRuleRemark remark, ExtendedRuleRemarkWrapper[] ruleRemarks) {
        for (ExtendedRuleRemarkWrapper ruleRemark : ruleRemarks) {
            if (ruleRemark.getWrappedRuleRemark().equals(remark)) {
                return ruleRemark;
            }
        }
        return null;
    }

}
