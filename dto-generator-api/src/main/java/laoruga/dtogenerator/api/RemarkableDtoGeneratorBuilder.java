package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.RuleRemark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RemarkableDtoGeneratorBuilder extends DtoGeneratorBuilder {

    Map<Class<? extends IGenerator<?>>, ExtendedRuleRemarkWrapper> extendedRuleRemarks = new HashMap<>();

    public DtoGeneratorBuilder addExtendedRuleRemarks(ExtendedRuleRemarkWrapper... ruleRemarks) {
        if (ruleRemarks != null && ruleRemarks.length != 0) {
            this.extendedRuleRemarks.putAll(
                    Arrays.stream(ruleRemarks).collect(
                            Collectors.toMap(
                                    ExtendedRuleRemarkWrapper::getGeneratorClass,
                                    wrappedRuleRemark -> wrappedRuleRemark
                            ))
            );
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
        return new RemarkableDtoGenerator(ruleRemark, fieldRuleRemarkMap, extendedRuleRemarks);
    }
}
