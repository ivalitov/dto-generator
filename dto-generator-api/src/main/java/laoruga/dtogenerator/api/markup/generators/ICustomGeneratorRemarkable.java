package laoruga.dtogenerator.api.markup.generators;

import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;

public interface ICustomGeneratorRemarkable<GENERATED_TYPE> extends ICustomGeneratorArgs<GENERATED_TYPE> {

    void setRuleRemarks(IExtendedRuleRemark... iRuleRemarks);

}
