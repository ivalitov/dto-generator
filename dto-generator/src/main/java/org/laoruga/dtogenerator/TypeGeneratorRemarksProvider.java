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

public class TypeGeneratorRemarksProvider {

    private final Map<String, IRuleRemark> basicRuleRemarksMapByField = new HashMap<>();
    private final AtomicReference<IRuleRemark> basicRuleRemarkForFields;
    private final Map<String, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByField = new HashMap<>();
    private final Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMap;

    public TypeGeneratorRemarksProvider() {
        this(new AtomicReference<>(), new ConcurrentHashMap<>());
    }

    private TypeGeneratorRemarksProvider(
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
            throw new DtoGeneratorException("Try to overwrite remark from: '" + getBasicRuleRemark(filedName) + "'" +
                    " to: '" + ruleRemark + "' for field '" + filedName + "'.");
        }
        basicRuleRemarksMapByField.put(filedName, ruleRemark);
    }

    public void setBasicRuleRemarkForFields(BasicRuleRemark basicRuleRemark) {
        if (basicRuleRemarkForFields.get() != null && basicRuleRemarkForFields.get() != basicRuleRemark) {
            throw new DtoGeneratorException("Try to overwrite remark for all fields from: '"
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

    boolean isCustomRuleRemarkExists(String fieldName) {
        return customRuleRemarksMapByField.containsKey(fieldName);
    }

    List<CustomRuleRemarkWrapper> getCustomRuleRemarks(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMap.get(customGenerator.getClass());
    }

    List<CustomRuleRemarkWrapper> getCustomRuleRemarks(String fieldName) {
        return customRuleRemarksMapByField.get(fieldName);
    }

    public Optional<List<CustomRuleRemarkWrapper>> getRemarks(String fieldName, ICustomGenerator<?> remarkableGenerator) {
        List<CustomRuleRemarkWrapper> mappedByField = isCustomRuleRemarkExists(fieldName) ?
                getCustomRuleRemarks(fieldName) : new ArrayList<>();
        List<CustomRuleRemarkWrapper> mappedByGenerator = isCustomRuleRemarkExists(remarkableGenerator) ?
                getCustomRuleRemarks(remarkableGenerator) : new ArrayList<>();
        if (mappedByGenerator.isEmpty() && mappedByField.isEmpty()) {
            return Optional.empty();
        }
        if (!mappedByField.isEmpty() && !mappedByGenerator.isEmpty()) {
            Iterator<CustomRuleRemarkWrapper> iterator = mappedByGenerator.iterator();
            while (iterator.hasNext()) {
                CustomRuleRemarkWrapper remarkMappedByGenerator = iterator.next();
                Optional<CustomRuleRemarkWrapper> sameRemark = mappedByField.stream()
                        .filter(i -> i.getWrappedRuleRemark().equals(remarkMappedByGenerator.getWrappedRuleRemark()))
                        .findAny();
                if (sameRemark.isPresent()) {
                    iterator.remove();
                }
            }
        }
        mappedByField.addAll(mappedByGenerator);
        return Optional.of(mappedByField);
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

    TypeGeneratorRemarksProvider copy() {
        return new TypeGeneratorRemarksProvider(basicRuleRemarkForFields, customRuleRemarksMap);
    }
}
