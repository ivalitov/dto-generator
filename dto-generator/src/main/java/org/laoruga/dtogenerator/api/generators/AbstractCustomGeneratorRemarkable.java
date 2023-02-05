package org.laoruga.dtogenerator.api.generators;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWithArgs;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;

import java.util.List;
import java.util.Optional;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCustomGeneratorRemarkable<T> implements ICustomGeneratorRemarkable<T> {

    private List<CustomRuleRemarkWithArgs> ruleRemarkWrapperList;

    @Override
    public final void setRuleRemarks(List<CustomRuleRemarkWithArgs> ruleRemarkWrapperList) {
        this.ruleRemarkWrapperList = ruleRemarkWrapperList;
    }

    protected Optional<CustomRuleRemarkWithArgs> getWrappedRemark(ICustomRuleRemark remark) {
        if (getRuleRemarkWrapperList() == null) {
            return Optional.empty();
        }
        for (CustomRuleRemarkWithArgs ruleRemark : getRuleRemarkWrapperList()) {
            if (ruleRemark.getCustomRuleRemark().equals(remark)) {
                return Optional.of(ruleRemark);
            }
        }
        return Optional.empty();
    }
}
