package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWrapper;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class TypeGeneratorRemarksProvider {

    private final Map<String, IRuleRemark> basicRuleRemarksMapByField;
    private final AtomicReference<IRuleRemark> basicRuleRemarkForAnyField;
    private final Map<String, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByField;
    private final Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByGenerator;

    public TypeGeneratorRemarksProvider() {
        this(new AtomicReference<>(), new HashMap<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    TypeGeneratorRemarksProvider(TypeGeneratorRemarksProvider toCopy) {
        this(toCopy.basicRuleRemarkForAnyField, toCopy.customRuleRemarksMapByGenerator);
    }

    private TypeGeneratorRemarksProvider(
            AtomicReference<IRuleRemark> basicRuleRemarkForAnyField,
            Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksMapByGenerator) {
        this.basicRuleRemarksMapByField = new HashMap<>();
        this.customRuleRemarksMapByField = new HashMap<>();
        this.basicRuleRemarkForAnyField = basicRuleRemarkForAnyField;
        this.customRuleRemarksMapByGenerator = customRuleRemarksMapByGenerator;
    }

    /*
     * Basic Rule Remarks
     */

    boolean isBasicRuleRemarkExists(String fieldName) {
        return basicRuleRemarksMapByField.containsKey(fieldName) || basicRuleRemarkForAnyField.get() != null;
    }

    void setBasicRuleRemarkForField(@NonNull String filedName,
                                    @NonNull RuleRemark ruleRemark) {
        if (basicRuleRemarksMapByField.containsKey(filedName)) {
            throw new DtoGeneratorException("Try to overwrite remark from: '" + getBasicRuleRemark(filedName) + "'" +
                    " to: '" + ruleRemark + "' for field '" + filedName + "'.");
        }
        basicRuleRemarksMapByField.put(filedName, ruleRemark);
    }

    void setBasicRuleRemarkForAnyField(RuleRemark basicRuleRemark) {
        if (basicRuleRemarkForAnyField.get() != null && basicRuleRemarkForAnyField.get() != basicRuleRemark) {
            throw new DtoGeneratorException("Try to overwrite remark for all fields from: '"
                    + basicRuleRemarkForAnyField.get() + "' to: '" + basicRuleRemark + "'.");
        }
        basicRuleRemarkForAnyField.set(basicRuleRemark);
    }

    IRuleRemark getBasicRuleRemark(String fieldName) throws NullPointerException {
        if (basicRuleRemarksMapByField.containsKey(fieldName)) {
            return basicRuleRemarksMapByField.get(fieldName);
        }
        return Objects.requireNonNull(basicRuleRemarkForAnyField.get());
    }

    /*
     * Custom Rule Remarks
     */

    boolean isCustomRuleRemarkExists(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMapByGenerator.containsKey(customGenerator.getClass());
    }

    boolean isCustomRuleRemarkExists(String fieldName) {
        return customRuleRemarksMapByField.containsKey(fieldName);
    }

    List<CustomRuleRemarkWrapper> getCustomRuleRemarks(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMapByGenerator.get(customGenerator.getClass());
    }

    List<CustomRuleRemarkWrapper> getCustomRuleRemarks(String fieldName) {
        return customRuleRemarksMapByField.get(fieldName);
    }

    public Optional<List<CustomRuleRemarkWrapper>> getCustomRuleRemarks(String fieldName, ICustomGenerator<?> remarkableGenerator) {
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

    void addCustomRuleRemarkForField(@NonNull String filedName,
                                     @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new LinkedList<>());
        for (CustomRuleRemarkWrapper remark : ruleRemark) {
            customRuleRemarksMapByField.get(filedName).add(remark);
        }
    }

    void addRuleRemarkForAllFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        for (CustomRuleRemarkWrapper remark : ruleRemarks) {
            this.customRuleRemarksMapByGenerator.putIfAbsent(remark.getGeneratorClass(), new LinkedList<>());
            this.customRuleRemarksMapByGenerator.get(remark.getGeneratorClass()).add(remark);
        }
    }
}
