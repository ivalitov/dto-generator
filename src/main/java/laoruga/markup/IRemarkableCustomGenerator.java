package laoruga.markup;

public interface IRemarkableCustomGenerator<GENERATED_TYPE> extends IGenerator<GENERATED_TYPE> {

    void setRuleRemarks(IExtendedRuleRemark... iRuleRemarks);

}
