package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkArgs;
import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class RemarkableDtoGenerator extends DtoGenerator {

    private final Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks;

    protected RemarkableDtoGenerator(IRuleRemark ruleRemark,
                                     Map<String, IRuleRemark> fieldRuleRemarkMap,
                                     Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> extendedRuleRemarks) {
        super(ruleRemark, fieldRuleRemarkMap);
        this.extendedRuleRemarks = extendedRuleRemarks;
    }

    public static RemarkableDtoGeneratorBuilder builder() {
        return new RemarkableDtoGeneratorBuilder();
    }

    @Override
    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = super.prepareGenerator(field);

        if (generator instanceof ICustomGeneratorRemarkable) {
            if (extendedRuleRemarks.containsKey(generator.getClass())) {
                IExtendedRuleRemark ruleRemark = extendedRuleRemarks.get(generator.getClass());
                if (ruleRemark instanceof ExtendedRuleRemarkArgs) {
                    ExtendedRuleRemarkArgs remarkWithArgs = (ExtendedRuleRemarkArgs) ruleRemark;
                    ((ICustomGeneratorRemarkable<?>) generator).setRuleRemarks(
                            remarkWithArgs.getWrappedRule()
                    );
                    if (remarkWithArgs.getArgs() != null && remarkWithArgs.getArgs().length != 0) {
                        log.debug("Annotation args have been overridden with remark {} passed values: {}",
                                ((ExtendedRuleRemarkArgs) ruleRemark).getWrappedRule(), remarkWithArgs.getArgs());
                        ((ICustomGeneratorRemarkable<?>) generator).setArgs(remarkWithArgs.getArgs());
                    }
                } else {
                    ((ICustomGeneratorRemarkable<?>) generator).setRuleRemarks(
                            ruleRemark
                    );
                }
            }
        }

        return generator;
    }
}
