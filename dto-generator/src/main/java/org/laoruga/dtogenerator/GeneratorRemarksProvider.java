package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWrapper;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class GeneratorRemarksProvider {

    private final Map<String, IRuleRemark> basicRuleRemarksMapByField = new HashMap<>();
    private final AtomicReference<IRuleRemark> basicRuleRemarkForFields;
    private final Map<String, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByField = new HashMap<>();
    private final Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMap;

    public GeneratorRemarksProvider() {
        this(new AtomicReference<>(), new ConcurrentHashMap<>());
    }

    private GeneratorRemarksProvider(
            AtomicReference<IRuleRemark> basicRuleRemarkForFields,
            Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMap) {
        this.basicRuleRemarkForFields = basicRuleRemarkForFields;
        this.customRuleRemarksMap = customRuleRemarksMap;
    }

    /*
     * Basic Rule Remarks
     */

    boolean isBasicRuleRemarkExists(String fieldName) {
        return basicRuleRemarksMapByField.containsKey(fieldName) || basicRuleRemarkForFields.get() != null;
    }

    void setBasicRuleRemarkForField(@NonNull String filedName,
                                    @NonNull BasicRuleRemark ruleRemark) {
        if (basicRuleRemarksMapByField.containsKey(filedName)) {
            throw new DtoGeneratorException("Trying to overwrite remark from: '" + getBasicRuleRemark(filedName) + "'" +
                    " to: '" + ruleRemark + "' for field '" + filedName + "'.");
        }
        basicRuleRemarksMapByField.put(filedName, ruleRemark);
    }

    public void setBasicRuleRemarkForFields(BasicRuleRemark basicRuleRemark) {
        if (basicRuleRemarkForFields.get() != null && basicRuleRemarkForFields.get() != basicRuleRemark) {
            throw new DtoGeneratorException("Trying to overwrite remark for all fields from: '"
                    + basicRuleRemarkForFields.get() + "' to: '" + basicRuleRemark + "'.");
        }
        basicRuleRemarkForFields.set(basicRuleRemark);
    }

    IRuleRemark getBasicRuleRemark(String fieldName) throws NullPointerException {
        if (basicRuleRemarksMapByField.containsKey(fieldName)) {
            return basicRuleRemarksMapByField.get(fieldName);
        }
        return Objects.requireNonNull(basicRuleRemarkForFields.get());
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

    GeneratorRemarksProvider copy() {
        return new GeneratorRemarksProvider(basicRuleRemarkForFields, customRuleRemarksMap);
    }
}
