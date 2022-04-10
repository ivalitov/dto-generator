package dtogenerator.api.factory;

import dtogenerator.api.markup.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RemarkableDtoBuilder extends DtoBuilder {

    Map<Class<? extends IGenerator<?>>, IExtendedRuleRemark> ruleRemarks = new HashMap<>();

    private RemarkableDtoBuilder() {
        // there is no reason to use empty constructor
    }

    public RemarkableDtoBuilder(IExtendedRuleRemark ruleRemark, IExtendedRuleRemark... ruleRemarks) {
        this.ruleRemarks.put(ruleRemark.getGeneratorClass(), ruleRemark);
        if (ruleRemarks != null && ruleRemarks.length != 0) {
         this.ruleRemarks.putAll(
                 Arrays.stream(ruleRemarks).collect(
                         Collectors.toMap(
                                 IExtendedRuleRemark::getGeneratorClass,
                                 k -> k
                         ))
         );
        }
    }

    public RemarkableDtoBuilder(List<IExtendedRuleRemark> ruleRemarks) {
        this(ruleRemarks.remove(0), ruleRemarks.toArray(new IExtendedRuleRemark[0]));
    }

    @Override
    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = super.prepareGenerator(field);

        if (generator instanceof IRemarkableCustomGenerator) {
            if (ruleRemarks.containsKey(generator.getClass())) {
                IExtendedRuleRemark ruleRemark = ruleRemarks.get(generator.getClass());
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
