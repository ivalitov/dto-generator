package dtogenerator.api;

import dtogenerator.api.markup.generators.IGenerator;
import dtogenerator.api.markup.generators.IRemarkableCustomGenerator;
import dtogenerator.api.markup.remarks.ExtendedRuleRemarkArgs;
import dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import dtogenerator.api.markup.remarks.IRuleRemark;
import dtogenerator.api.markup.remarks.RuleRemark;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RemarkableDtoGenerator extends DtoGenerator {

    private final Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks;

    protected RemarkableDtoGenerator(Set<IRuleRemark> ruleRemarks,
                                  Map<String, IRuleRemark> fieldRuleRemarkMap,
                                  Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks) {
        super(ruleRemarks, fieldRuleRemarkMap);
        this.extendedRuleRemarks = extendedRuleRemarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DtoGenerator.Builder {

        Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks = new HashMap<>();

        public DtoGenerator.Builder addRuleRemarks(IExtendedRuleRemark ruleRemark, IExtendedRuleRemark... ruleRemarks) {
            this.extendedRuleRemarks.put(ruleRemark.getGeneratorClass(), ruleRemark);
            if (ruleRemarks != null && ruleRemarks.length != 0) {
                this.extendedRuleRemarks.putAll(
                        Arrays.stream(ruleRemarks).collect(
                                Collectors.toMap(
                                        IExtendedRuleRemark::getGeneratorClass,
                                        k -> k
                                ))
                );
            }
            return this;
        }

        @Override
        public Builder addRuleRemarks(RuleRemark ruleRemark, IRuleRemark... ruleRemarks) {
            super.addRuleRemarks(ruleRemark, ruleRemarks);
            return this;
        }

        @Override
        public Builder addRuleRemarks(String filedName, IRuleRemark ruleRemark) {
            super.addRuleRemarks(filedName, ruleRemark);
            return this;
        }

        public RemarkableDtoGenerator build() {
            return new RemarkableDtoGenerator(ruleRemarks, fieldRuleRemarkMap, extendedRuleRemarks);
        }
    }

    @Override
    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = super.prepareGenerator(field);

        if (generator instanceof IRemarkableCustomGenerator) {
            if (extendedRuleRemarks.containsKey(generator.getClass())) {
                IExtendedRuleRemark ruleRemark = extendedRuleRemarks.get(generator.getClass());
                if (ruleRemark instanceof ExtendedRuleRemarkArgs) {
                    ExtendedRuleRemarkArgs remarkWithArgs = (ExtendedRuleRemarkArgs) ruleRemark;
                    ((IRemarkableCustomGenerator<?>) generator).setRuleRemarks(
                            remarkWithArgs.getWrappedRule()
                    );
                    if (remarkWithArgs.getArgs() != null && remarkWithArgs.getArgs().length != 0) {
                        log.debug("Annotation args have been overridden with remark {} passed values: {}",
                                ((ExtendedRuleRemarkArgs) ruleRemark).getWrappedRule(), remarkWithArgs.getArgs());
                        ((IRemarkableCustomGenerator<?>) generator).setArgs(remarkWithArgs.getArgs());
                    }
                } else {
                    ((IRemarkableCustomGenerator<?>) generator).setRuleRemarks(
                            ruleRemark
                    );
                }
            }
        }

        return generator;
    }
}
