package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;
import org.laoruga.dtogenerator.util.dummy.DummyCustomGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class RemarksHolderCustom {

    private final Map<String, Set<CustomRuleRemark>> customRuleRemarksMapByField;
    private final Map<Class<? extends CustomGenerator<?>>, Set<CustomRuleRemark>> customRuleRemarksMapByGenerator;

    // Stubs
    private static final Set<CustomRuleRemark> EMPTY_SET = Collections.unmodifiableSet(new HashSet<>());
    private static final Map<CustomRuleRemark, CustomRuleRemarkArgs> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<>());

    public RemarksHolderCustom() {
        this(new HashMap<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    RemarksHolderCustom(RemarksHolderCustom toCopy) {
        this(
                toCopy.customRuleRemarksMapByGenerator.entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> new HashSet<>(entry.getValue())
                                )
                        )
        );

    }

    private RemarksHolderCustom(
            Map<Class<? extends CustomGenerator<?>>, Set<CustomRuleRemark>> customRuleRemarksMapByGenerator) {
        this.customRuleRemarksMapByField = new HashMap<>();
        this.customRuleRemarksMapByGenerator = customRuleRemarksMapByGenerator;
    }

    /*
     * Getters and adders
     */

    void addRemark(@NonNull String filedName,
                   @NonNull CustomRuleRemark ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new HashSet<>());
        customRuleRemarksMapByField.get(filedName).add(ruleRemark);
    }

    void addRemarkForAnyField(@NonNull Class<? extends CustomGenerator<?>> generatorClass,
                              @NonNull CustomRuleRemark ruleRemarks) {
        customRuleRemarksMapByGenerator.putIfAbsent(generatorClass, new HashSet<>());
        customRuleRemarksMapByGenerator.get(generatorClass).add(ruleRemarks);
    }

    public Set<CustomRuleRemark> getRemarks(String fieldName,
                                            Class<?> remarkableGeneratorClass) {
        Set<CustomRuleRemark> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;

        Set<CustomRuleRemark> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;

        Set<CustomRuleRemark> mappedByAnyGenerator =
                isCustomRuleRemarkExists(DummyCustomGenerator.class) ? getRemarks(DummyCustomGenerator.class) : null;

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

        Set<CustomRuleRemark> remarksSet = new HashSet<>();

        if (mappedByGenerator != null) {
            remarksSet.addAll(mappedByGenerator);
        }
        if (mappedByField != null) {
            remarksSet.addAll(mappedByField);
        }
        return remarksSet;
    }

    public Map<CustomRuleRemark, CustomRuleRemarkArgs> getRemarksWithArgs(String fieldName,
                                                                          Class<?> remarkableGeneratorClass) {
        Set<CustomRuleRemark> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;

        Set<CustomRuleRemark> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;

        Set<CustomRuleRemark> mappedByAnyGenerator =
                isCustomRuleRemarkExists(DummyCustomGenerator.class) ? getRemarks(DummyCustomGenerator.class) : null;

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

        Map<CustomRuleRemark, CustomRuleRemarkArgs> remarksMap = new HashMap<>();

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

    private Set<CustomRuleRemark> getRemarks(Class<?> customGeneratorClass) {
        return customRuleRemarksMapByGenerator.get(customGeneratorClass);
    }

    private Set<CustomRuleRemark> getRemarks(String fieldName) {
        return customRuleRemarksMapByField.get(fieldName);
    }

    private boolean isCustomRuleRemarkExists(Class<?> customGenerator) {
        return customRuleRemarksMapByGenerator.containsKey(customGenerator);
    }

    private boolean isCustomRuleRemarkExists(String fieldName) {
        return customRuleRemarksMapByField.containsKey(fieldName);
    }

    private void addToMap(Map<CustomRuleRemark, CustomRuleRemarkArgs> remarksMap, Set<CustomRuleRemark> remarksSet) {
        for (CustomRuleRemark remark : remarksSet) {

            if (remark instanceof CustomRuleRemarkArgs) {

                remarksMap.put(
                        ((CustomRuleRemarkArgs) remark).getRemarkInstance(),
                        ((CustomRuleRemarkArgs) remark));
            } else {

                remarksMap.put(remark, () -> 0);
            }

        }
    }

}
