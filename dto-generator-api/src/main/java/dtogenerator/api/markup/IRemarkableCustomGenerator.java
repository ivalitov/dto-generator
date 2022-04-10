package dtogenerator.api.markup;

public interface IRemarkableCustomGenerator<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setRuleRemarks(IExtendedRuleRemark... iRuleRemarks);

}
