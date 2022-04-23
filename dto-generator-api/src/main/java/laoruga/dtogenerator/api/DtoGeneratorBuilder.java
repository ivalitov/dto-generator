package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.RuleRemark;

import java.util.HashMap;
import java.util.Map;

public class DtoGeneratorBuilder {

    protected IRuleRemark ruleRemark = RuleRemark.RANDOM_VALUE;
    protected final Map<String, IRuleRemark> fieldRuleRemarkMap = new HashMap<>();

    public DtoGeneratorBuilder setRuleRemark(RuleRemark ruleRemark) {
        this.ruleRemark = ruleRemark;
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarks(String filedName, IRuleRemark ruleRemark) {
        fieldRuleRemarkMap.put(filedName, ruleRemark);
        return this;
    }

    public DtoGenerator build() {
        return new DtoGenerator(ruleRemark, fieldRuleRemarkMap, this);
    }
}
