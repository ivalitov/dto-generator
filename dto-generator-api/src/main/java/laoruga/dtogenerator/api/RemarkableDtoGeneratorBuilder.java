package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;

import java.util.*;

public class RemarkableDtoGeneratorBuilder extends DtoGeneratorBuilder {

    Map<Class<? extends IGenerator<?>>, List<CustomRuleRemarkWrapper>> extendedRuleRemarks = new HashMap<>();

    public DtoGeneratorBuilder addRuleRemarkForField(CustomRuleRemarkWrapper... ruleRemarks) {
        if (ruleRemarks != null && ruleRemarks.length != 0) {
            for (CustomRuleRemarkWrapper remark : ruleRemarks) {
                this.extendedRuleRemarks.putIfAbsent(remark.getGeneratorClass(), new LinkedList<>());
                this.extendedRuleRemarks.get(remark.getGeneratorClass()).add(remark);
            }
        }
        return this;
    }

    @Override
    public RemarkableDtoGeneratorBuilder setRuleRemarkForAllFields(BasicRuleRemark basicRuleRemark) {
        super.setRuleRemarkForAllFields(basicRuleRemark);
        return this;
    }

    @Override
    public RemarkableDtoGeneratorBuilder addRuleRemarkForField(String filedName, IRuleRemark ruleRemark) {
        super.addRuleRemarkForField(filedName, ruleRemark);
        return this;
    }

    public RemarkableDtoGenerator build() {
        return new RemarkableDtoGenerator(ruleRemark, fieldRuleRemarkMap, extendedRuleRemarks, this);
    }
}
