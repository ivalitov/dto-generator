package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;

import java.util.HashMap;
import java.util.Map;

public class DtoGeneratorBuilder {

    protected IRuleRemark ruleRemark = BasicRuleRemark.RANDOM_VALUE;
    protected final Map<String, IRuleRemark> fieldRuleRemarkMap = new HashMap<>();

    public DtoGeneratorBuilder setRuleRemarkForAllFields(BasicRuleRemark basicRuleRemark) {
        this.ruleRemark = basicRuleRemark;
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarkForField(String filedName, IRuleRemark ruleRemark) {
        fieldRuleRemarkMap.put(filedName, ruleRemark);
        return this;
    }

    public DtoGenerator build() {
        return new DtoGenerator(ruleRemark, fieldRuleRemarkMap, this);
    }
}
