package laoruga.dtogenerator.api.markup.generators;

import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCustomGeneratorRemarkable<T> implements ICustomGeneratorRemarkable<T> {

    private List<CustomRuleRemarkWrapper> ruleRemarkWrapperList;

    @Override
    final public void setRuleRemarks(List<CustomRuleRemarkWrapper> ruleRemarkWrapperList) {
        this.ruleRemarkWrapperList = ruleRemarkWrapperList;
    }

    protected Optional<CustomRuleRemarkWrapper> getWrappedRemark(ICustomRuleRemark remark) {
        if (getRuleRemarkWrapperList() == null) {
            return Optional.empty();
        }
        for (CustomRuleRemarkWrapper ruleRemark : getRuleRemarkWrapperList()) {
            if (ruleRemark.getWrappedRuleRemark().equals(remark)) {
                return Optional.of(ruleRemark);
            }
        }
        return Optional.empty();
    }
}
