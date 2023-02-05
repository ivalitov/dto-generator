package org.laoruga.dtogenerator.api.remarks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@AllArgsConstructor
@Getter
public class CustomRuleRemarkWithArgs implements ICustomRuleRemark {

    private ICustomRuleRemark customRuleRemark;
    private String[] args;

    @Override
    public CustomRuleRemarkWithArgs setArgs(String... args) {
        return customRuleRemark.setArgs(args);
    }

    @Override
    public int requiredArgsNumber() {
        return customRuleRemark.requiredArgsNumber();
    }

    @Override
    public Class<? extends ICustomGenerator<?>> getGeneratorClass() {
        return customRuleRemark.getGeneratorClass();
    }

    @Override
    public ICustomRuleRemark getRemarkInstance() {
        return customRuleRemark;
    }

    @Override
    public boolean equals(Object o) {
        return customRuleRemark.equals(o);
    }

    @Override
    public int hashCode() {
        return customRuleRemark.hashCode();
    }
}