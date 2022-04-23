package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

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
            ICustomGeneratorRemarkable<?> remarkableGenerator = (ICustomGeneratorRemarkable<?>) generator;
            if (extendedRuleRemarks.containsKey(remarkableGenerator.getClass())) {
                IExtendedRuleRemark remark = extendedRuleRemarks.get(remarkableGenerator.getClass());
                if (remark.getArgs().length != remark.requiredArgsNumber()) {
                    throw new DtoGeneratorException("Remark '" + remark + "' expected '" + remark.requiredArgsNumber() +
                            "' args. Passed '" + remark.getArgs().length + " args'");
                }
                remarkableGenerator.setRuleRemarks(remark);
            }
        }
        return generator;
    }
}
