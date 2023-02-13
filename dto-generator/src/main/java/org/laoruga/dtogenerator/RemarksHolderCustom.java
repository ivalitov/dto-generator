package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomGeneratorStub;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemarkArgs;

import java.util.*;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class RemarksHolderCustom {

    private final Map<String, Set<ICustomRuleRemark>> customRuleRemarksMapByField;
    private final Map<Class<? extends ICustomGenerator<?>>, Set<ICustomRuleRemark>> customRuleRemarksMapByGenerator;

    // Stubs
    private static final Set<ICustomRuleRemark> EMPTY_SET = Collections.unmodifiableSet(new HashSet<>());
    private static final Map<ICustomRuleRemark, ICustomRuleRemarkArgs> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<>());

    public RemarksHolderCustom() {
        this(new HashMap<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    RemarksHolderCustom(RemarksHolderCustom toCopy) {
        this(toCopy.customRuleRemarksMapByGenerator);
    }

    private RemarksHolderCustom(
            Map<Class<? extends ICustomGenerator<?>>, Set<ICustomRuleRemark>> customRuleRemarksMapByGenerator) {
        this.customRuleRemarksMapByField = new HashMap<>();
        this.customRuleRemarksMapByGenerator = customRuleRemarksMapByGenerator;
    }

    /*
     * Getters and adders
     */

    void addRemark(@NonNull String filedName,
                   @NonNull ICustomRuleRemark ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new HashSet<>());
        customRuleRemarksMapByField.get(filedName).add(ruleRemark);
    }

    void addRemarkForAnyField(@NonNull ICustomRuleRemark ruleRemarks) {
        customRuleRemarksMapByGenerator.putIfAbsent(ruleRemarks.getGeneratorClass(), new HashSet<>());
        customRuleRemarksMapByGenerator.get(ruleRemarks.getGeneratorClass()).add(ruleRemarks);
    }

    public Set<ICustomRuleRemark> getRemarks(String fieldName,
                                             Class<?> remarkableGeneratorClass) {
        Set<ICustomRuleRemark> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;

        Set<ICustomRuleRemark> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;

        Set<ICustomRuleRemark> mappedByAnyGenerator =
                isCustomRuleRemarkExists(CustomGeneratorStub.class) ? getRemarks(CustomGeneratorStub.class) : null;

        if (mappedByAnyGenerator != null) {
            if (mappedByGenerator == null) {
                mappedByGenerator = mappedByAnyGenerator;
            } else {
                mappedByGenerator.addAll(mappedByAnyGenerator);
            }
        }

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

    public Map<ICustomRuleRemark, ICustomRuleRemarkArgs> getRemarksWithArgs(String fieldName,
                                                                            Class<?> remarkableGeneratorClass) {
        Set<ICustomRuleRemark> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;

        Set<ICustomRuleRemark> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;

        Set<ICustomRuleRemark> mappedByAnyGenerator =
                isCustomRuleRemarkExists(CustomGeneratorStub.class) ? getRemarks(CustomGeneratorStub.class) : null;

        if (mappedByAnyGenerator != null) {
            if (mappedByGenerator == null) {
                mappedByGenerator = mappedByAnyGenerator;
            } else {
                mappedByGenerator.addAll(mappedByAnyGenerator);
            }
        }

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

    /*
     * Private utils
     */

    private Set<ICustomRuleRemark> getRemarks(Class<?> customGeneratorClass) {
        return customRuleRemarksMapByGenerator.get(customGeneratorClass);
    }

    private Set<ICustomRuleRemark> getRemarks(String fieldName) {
        return customRuleRemarksMapByField.get(fieldName);
    }

    private boolean isCustomRuleRemarkExists(Class<?> customGenerator) {
        return customRuleRemarksMapByGenerator.containsKey(customGenerator);
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
