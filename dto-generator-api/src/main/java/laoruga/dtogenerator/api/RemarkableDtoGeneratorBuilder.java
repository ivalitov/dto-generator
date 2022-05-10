package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.RuleRemark;

import java.util.*;
import java.util.stream.Collectors;

public class RemarkableDtoGeneratorBuilder extends DtoGeneratorBuilder {

    Map<Class<? extends IGenerator<?>>, List<ExtendedRuleRemarkWrapper>> extendedRuleRemarks = new HashMap<>();

    public DtoGeneratorBuilder addExtendedRuleRemarks(ExtendedRuleRemarkWrapper... ruleRemarks) {
        if (ruleRemarks != null && ruleRemarks.length != 0) {
            for (ExtendedRuleRemarkWrapper remark : ruleRemarks) {
                this.extendedRuleRemarks.putIfAbsent(remark.getGeneratorClass(), new LinkedList<>());
                this.extendedRuleRemarks.get(remark.getGeneratorClass()).add(remark);
            }
        }
        return this;
    }

    @Override
    public RemarkableDtoGeneratorBuilder setRuleRemark(RuleRemark ruleRemark) {
        super.setRuleRemark(ruleRemark);
        return this;
    }

    @Override
    public RemarkableDtoGeneratorBuilder addRuleRemarks(String filedName, IRuleRemark ruleRemark) {
        super.addRuleRemarks(filedName, ruleRemark);
        return this;
    }

    public RemarkableDtoGenerator build() {
        return new RemarkableDtoGenerator(ruleRemark, fieldRuleRemarkMap, extendedRuleRemarks, this);
    }
}
