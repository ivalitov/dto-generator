package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemarkArgs;
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
    private final Map<String, Set<ICustomRuleRemark>> customRuleRemarksMapByField;
    private final Map<Class<? extends ICustomGenerator<?>>, Set<ICustomRuleRemark>> customRuleRemarksMapByGenerator;

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
            Map<Class<? extends ICustomGenerator<?>>, Set<ICustomRuleRemark>> customRuleRemarksMapByGenerator) {
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

    void addCustomRuleRemarkForField(@NonNull String filedName,
                                     @NonNull ICustomRuleRemark ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new HashSet<>());
        customRuleRemarksMapByField.get(filedName).add(ruleRemark);
    }

    void addRuleRemarkForAllFields(@NonNull ICustomRuleRemark ruleRemarks) {
        this.customRuleRemarksMapByGenerator.putIfAbsent(ruleRemarks.getGeneratorClass(), new HashSet<>());
        this.customRuleRemarksMapByGenerator.get(ruleRemarks.getGeneratorClass()).add(ruleRemarks);
    }

    private static final Set<ICustomRuleRemark> EMPTY_SET = Collections.unmodifiableSet(new HashSet<>());
    private static final Map<ICustomRuleRemark, ICustomRuleRemarkArgs> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<>());

    public Set<ICustomRuleRemark> getCustomRuleRemarks(String fieldName,
                                                       ICustomGenerator<?> remarkableGenerator) {
        Set<ICustomRuleRemark> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getCustomRuleRemarks(fieldName) : null;

        Set<ICustomRuleRemark> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGenerator) ? getCustomRuleRemarks(remarkableGenerator) : null;

        if (mappedByField == null && mappedByGenerator == null) {
            return EMPTY_SET;
        }

        Set<ICustomRuleRemark> remarksSet = new HashSet<>();

        if (mappedByGenerator != null) {
            remarksSet.addAll(mappedByGenerator);
        }
        if (mappedByField != null) {
            remarksSet.addAll(mappedByField);
        }
        return remarksSet;
    }

    public Map<ICustomRuleRemark, ICustomRuleRemarkArgs> getCustomRuleRemarksArgs(String fieldName,
                                                                                  ICustomGenerator<?> remarkableGenerator) {
        Set<ICustomRuleRemark> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getCustomRuleRemarks(fieldName) : null;

        Set<ICustomRuleRemark> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGenerator) ? getCustomRuleRemarks(remarkableGenerator) : null;

        if (mappedByField == null && mappedByGenerator == null) {
            return EMPTY_MAP;
        }

        Map<ICustomRuleRemark, ICustomRuleRemarkArgs> remarksMap = new HashMap<>();

        if (mappedByGenerator != null) {
            addToMap(remarksMap, mappedByGenerator);
        }
        if (mappedByField != null) {
            addToMap(remarksMap, mappedByField);
        }
        return remarksMap;
    }

    private Set<ICustomRuleRemark> getCustomRuleRemarks(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMapByGenerator.get(customGenerator.getClass());
    }

    private Set<ICustomRuleRemark> getCustomRuleRemarks(String fieldName) {
        return customRuleRemarksMapByField.get(fieldName);
    }

    private boolean isCustomRuleRemarkExists(ICustomGenerator<?> customGenerator) {
        return customRuleRemarksMapByGenerator.containsKey(customGenerator.getClass());
    }

    private boolean isCustomRuleRemarkExists(String fieldName) {
        return customRuleRemarksMapByField.containsKey(fieldName);
    }

    private void addToMap(Map<ICustomRuleRemark, ICustomRuleRemarkArgs> remarksMap, Set<ICustomRuleRemark> remarksSet) {
        for (ICustomRuleRemark remark : remarksSet) {
            if (!(remark instanceof ICustomRuleRemarkArgs)) {
                final ICustomRuleRemark remarkFinal = remark;
                final ICustomRuleRemarkArgs wrappedRemark = new ICustomRuleRemarkArgs() {
                    @Override
                    public int requiredArgsNumber() {
                        return 0;
                    }

                    @Override
                    public Class<? extends ICustomGenerator<?>> getGeneratorClass() {
                        return remarkFinal.getGeneratorClass();
                    }
                };
                remarksMap.put(remarkFinal, wrappedRemark);
            } else {
                remarksMap.put(
                        ((ICustomRuleRemarkArgs) remark).getRemarkInstance(),
                        ((ICustomRuleRemarkArgs) remark));
            }
        }
    }

}
