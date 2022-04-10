package dtogenerator.api;

import dtogenerator.api.markup.remarks.IRuleRemark;
import dtogenerator.api.markup.remarks.RuleRemark;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DtoGeneratorBuilder {

    protected final Set<IRuleRemark> ruleRemarks = new HashSet<>();
    protected final Map<String, IRuleRemark> fieldRuleRemarkMap = new HashMap<>();

    public DtoGeneratorBuilder addRuleRemarks(RuleRemark ruleRemark, IRuleRemark... ruleRemarks) {
        this.ruleRemarks.add(ruleRemark);
        if (ruleRemarks.length > 0) {
            for (IRuleRemark remark : ruleRemarks) {
                this.ruleRemarks.add(remark);
            }
        }
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarks(String filedName, IRuleRemark ruleRemark) {
        fieldRuleRemarkMap.put(filedName, ruleRemark);
        return this;
    }

    public DtoGenerator build() {
        return new DtoGenerator(ruleRemarks, fieldRuleRemarkMap);
    }
}
