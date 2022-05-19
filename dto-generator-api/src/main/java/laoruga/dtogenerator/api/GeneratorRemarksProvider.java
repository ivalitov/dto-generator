package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeneratorRemarksProvider {

    private final Map<String, IRuleRemark> basicRuleRemarksMapByField = new HashMap<>();
    private final Map<String, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByField = new HashMap<>();
    private final Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMap= new HashMap<>();

    IRuleRemark getBasicRuleRemark(String fieldName) {
        if (basicRuleRemarksMapByField.containsKey(fieldName)) {
            return basicRuleRemarksMapByField.get(fieldName);
        }
        return Objects.requireNonNull(basicRuleRemarksMapByField.get(null));
    }

    boolean isCustomRemarkExists(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMap.containsKey(customGenerator.getClass());
    }

    List<CustomRuleRemarkWrapper> getCustomRemarks(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMap.get(customGenerator.getClass());
    }

}
