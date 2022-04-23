package laoruga.dtogenerator.api.markup.generators;

import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;

public interface ICustomGeneratorRemarkable<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(ExtendedRuleRemarkWrapper... iRuleRemarks);

}
