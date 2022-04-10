package dtogenerator.api;

import dtogenerator.api.markup.generators.IGenerator;
import dtogenerator.api.markup.generators.IRemarkableCustomGenerator;
import dtogenerator.api.markup.remarks.ExtendedRuleRemarkArgs;
import dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class RemarkableDtoGenerator extends DtoGenerator {

    private final Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks;

    protected RemarkableDtoGenerator(Set<IRuleRemark> ruleRemarks,
                                  Map<String, IRuleRemark> fieldRuleRemarkMap,
                                  Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks) {
        super(ruleRemarks, fieldRuleRemarkMap);
        this.extendedRuleRemarks = extendedRuleRemarks;
    }

    public static RemarkableDtoGeneratorBuilder builder() {
        return new RemarkableDtoGeneratorBuilder();
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
