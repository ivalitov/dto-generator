package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GeneratorRemarksProvider {

    private final Map<String, IRuleRemark> basicRuleRemarksMapByField = new HashMap<>();
    private final Map<String, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByField = new HashMap<>();
    @Getter(AccessLevel.PACKAGE)
    private final Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMap;

    public GeneratorRemarksProvider() {
        customRuleRemarksMap = new ConcurrentHashMap<>();
    }

    public GeneratorRemarksProvider(Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMap) {
        this.customRuleRemarksMap = customRuleRemarksMap;
    }

    /*
     * Basic Rule Remarks
     */

    boolean isBasicRuleRemarkExists(String fieldName) {
        return basicRuleRemarksMapByField.containsKey(fieldName) || basicRuleRemarksMapByField.containsKey(null);
    }

    void setBasicRuleRemarkForField(String filedName,
                                    @NonNull BasicRuleRemark ruleRemark) {
        if (basicRuleRemarksMapByField.containsKey(filedName)) {
            if (filedName != null) {
                throw new DtoGeneratorException("Trying to overwrite remark from: '" + getBasicRuleRemark(filedName) + "'" +
                        " to: '" + ruleRemark + "' for field '" + filedName + "'.");
            } else {
                throw new DtoGeneratorException("Trying to overwrite remark from: '" + getBasicRuleRemark(null) + "'" +
                        " to: '" + ruleRemark + "' for all fields.");
            }
        }
        basicRuleRemarksMapByField.put(filedName, ruleRemark);
    }

    IRuleRemark getBasicRuleRemark(String fieldName) {
        if (basicRuleRemarksMapByField.containsKey(fieldName)) {
            return basicRuleRemarksMapByField.get(fieldName);
        }
        return Objects.requireNonNull(basicRuleRemarksMapByField.get(null));
    }

    /*
     * Custom Rule Remarks
     */

    boolean isCustomRuleRemarkExists(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMap.containsKey(customGenerator.getClass());
    }

    List<CustomRuleRemarkWrapper> getCustomRuleRemarks(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMap.get(customGenerator.getClass());
    }

    public void addCustomRuleRemarkForField(@NonNull String filedName,
                                            @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new LinkedList<>());
        for (CustomRuleRemarkWrapper remark : ruleRemark) {
            customRuleRemarksMapByField.get(filedName).add(remark);
        }
    }

    public void addRuleRemarkForAllFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        for (CustomRuleRemarkWrapper remark : ruleRemarks) {
            this.customRuleRemarksMap.putIfAbsent(remark.getGeneratorClass(), new LinkedList<>());
            this.customRuleRemarksMap.get(remark.getGeneratorClass()).add(remark);
        }
    }
}
