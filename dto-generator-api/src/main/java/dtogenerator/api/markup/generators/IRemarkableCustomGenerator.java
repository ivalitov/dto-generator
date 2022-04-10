package dtogenerator.api.markup.generators;

import dtogenerator.api.markup.remarks.IExtendedRuleRemark;

public interface IRemarkableCustomGenerator<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(IExtendedRuleRemark... iRuleRemarks);

}
