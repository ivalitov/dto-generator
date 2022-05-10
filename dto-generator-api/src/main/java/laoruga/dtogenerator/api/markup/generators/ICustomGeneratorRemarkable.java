package laoruga.dtogenerator.api.markup.generators;

import com.sun.istack.internal.Nullable;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;

import java.util.List;

public interface ICustomGeneratorRemarkable<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(List<ExtendedRuleRemarkWrapper> iRuleRemarks);

    @Nullable
    static ExtendedRuleRemarkWrapper getRemarkOrNull(IExtendedRuleRemark remark, List<ExtendedRuleRemarkWrapper> ruleRemarks) {
        for (ExtendedRuleRemarkWrapper ruleRemark : ruleRemarks) {
            if (ruleRemark.getWrappedRuleRemark().equals(remark)) {
                return ruleRemark;
            }
        }
        return null;
    }

}
