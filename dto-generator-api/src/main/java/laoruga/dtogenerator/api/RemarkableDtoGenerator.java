package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Slf4j
public class RemarkableDtoGenerator extends DtoGenerator {

    private final Map<Class<? extends IGenerator<?>>, List<CustomRuleRemarkWrapper>> extendedRuleRemarks;

    protected RemarkableDtoGenerator(Map<String, IRuleRemark> fieldRuleRemarkMap,
                                     Map<Class<? extends IGenerator<?>>, List<CustomRuleRemarkWrapper>> extendedRuleRemarks,
                                     RemarkableDtoGeneratorBuilder remarkableDtoGeneratorBuilder) {
        super(fieldRuleRemarkMap, remarkableDtoGeneratorBuilder);
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
                remarkableGenerator.setRuleRemarks(extendedRuleRemarks.get(remarkableGenerator.getClass()));
            }
        }
        return generator;
    }
}
