package org.laoruga.dtogenerator.api.remarks;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@AllArgsConstructor
@Getter
public class CustomRuleRemarkWithArgs implements CustomRuleRemarkArgs {

    private CustomRuleRemarkArgs customRuleRemark;
    private String[] args;

    @Override
    public CustomRuleRemarkWithArgs setArgs(String... args) {
        return customRuleRemark.setArgs(args);
    }

    @Override
    public int minimumArgsNumber() {
        return customRuleRemark.minimumArgsNumber();
    }

//    @Override
//    public Class<? extends CustomGenerator<?>> getGeneratorClass() {
//        return customRuleRemark.getGeneratorClass();
//    }

    @Override
    public CustomRuleRemarkArgs getRemarkInstance() {
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