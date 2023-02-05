package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWithArgs;
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
    private final Map<String, List<CustomRuleRemarkWithArgs>> customRuleRemarksMapByField;
    private final Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWithArgs>> customRuleRemarksMapByGenerator;

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
            Map<Class<? extends ICustomGenerator<?>>, List<CustomRuleRemarkWithArgs>> customRuleRemarksMapByGenerator) {
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

    List<CustomRuleRemarkWithArgs> getCustomRuleRemarks(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMapByGenerator.get(customGenerator.getClass());
    }

    List<CustomRuleRemarkWithArgs> getCustomRuleRemarks(String fieldName) {
        return customRuleRemarksMapByField.get(fieldName);
    }

    public Optional<List<CustomRuleRemarkWithArgs>> getCustomRuleRemarks(String fieldName, ICustomGenerator<?> remarkableGenerator) {
        List<CustomRuleRemarkWithArgs> mappedByField = isCustomRuleRemarkExists(fieldName) ?
                getCustomRuleRemarks(fieldName) : new ArrayList<>();
        List<CustomRuleRemarkWithArgs> mappedByGenerator = isCustomRuleRemarkExists(remarkableGenerator) ?
                getCustomRuleRemarks(remarkableGenerator) : new ArrayList<>();
        if (mappedByGenerator.isEmpty() && mappedByField.isEmpty()) {
            return Optional.empty();
        }
        if (!mappedByField.isEmpty() && !mappedByGenerator.isEmpty()) {
            Iterator<CustomRuleRemarkWithArgs> iterator = mappedByGenerator.iterator();
            while (iterator.hasNext()) {
                CustomRuleRemarkWithArgs remarkMappedByGenerator = iterator.next();
                Optional<CustomRuleRemarkWithArgs> sameRemark = mappedByField.stream()
                        .filter(i -> i.getCustomRuleRemark().equals(remarkMappedByGenerator.getCustomRuleRemark()))
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
                                     @NonNull CustomRuleRemarkWithArgs ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new LinkedList<>());
        customRuleRemarksMapByField.get(filedName).add(ruleRemark);
    }

    void addRuleRemarkForAllFields(@NonNull CustomRuleRemarkWithArgs ruleRemarks) {
        this.customRuleRemarksMapByGenerator.putIfAbsent(ruleRemarks.getGeneratorClass(), new LinkedList<>());
        this.customRuleRemarksMapByGenerator.get(ruleRemarks.getGeneratorClass()).add(ruleRemarks);
    }
}
