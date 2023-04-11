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
    private final Map<String, Map<String, String>> customRuleRemarksMapByField_;
//    private final Map<Class<? extends CustomGenerator<?>>, Set<CustomRuleRemark>> customRuleRemarksMapByGenerator;
    private final Map<Class<? extends CustomGenerator<?>>, Map<String, String>> customRuleRemarksMapByGenerator_;

    // Stubs
    private static final Set<CustomRuleRemark> EMPTY_SET = Collections.unmodifiableSet(new HashSet<>());
    private static final Map<String, String> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<>());

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
                toCopy.customRuleRemarksMapByGenerator_.entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> new HashMap<>(entry.getValue())
                                )
                        )
        );

    }

    private RemarksHolderCustom(
            Map<Class<? extends CustomGenerator<?>>, Map<String, String>> customRuleRemarksMapByGenerator) {
        this.customRuleRemarksMapByField = new HashMap<>();
        this.customRuleRemarksMapByField_ = new HashMap<>();
        this.customRuleRemarksMapByGenerator_ = customRuleRemarksMapByGenerator;
    }

    /*
     * Getters and adders
     */

    void addRemark(@NonNull String filedName,
                   @NonNull CustomRuleRemark ruleRemark) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new HashSet<>());
        customRuleRemarksMapByField.get(filedName).add(ruleRemark);
    }

    void addParameterForField(@NonNull String filedName,
                              @NonNull String remarkName,
                              @NonNull String remarkValue) {
        customRuleRemarksMapByField_.putIfAbsent(filedName, new HashMap<>());
        customRuleRemarksMapByField_.get(filedName).put(remarkName, remarkValue);
    }

    void addRemarkForAnyField(@NonNull Class<? extends CustomGenerator<?>> generatorClass,
                              @NonNull CustomRuleRemark ruleRemarks) {
//        customRuleRemarksMapByGenerator.putIfAbsent(generatorClass, new HashSet<>());
//        customRuleRemarksMapByGenerator.get(generatorClass).add(ruleRemarks);
    }

    void addParameterForGeneratorType(@NonNull Class<? extends CustomGenerator<?>> generatorClass,
                                      @NonNull String key,
                                      @NonNull String value) {
        customRuleRemarksMapByGenerator_.putIfAbsent(generatorClass, new HashMap<>());
        customRuleRemarksMapByGenerator_.get(generatorClass).put(key, value);
    }

    public Set<CustomRuleRemark> getRemarks(String fieldName,
                                            Class<?> remarkableGeneratorClass) {
//        Set<CustomRuleRemark> mappedByField =
//                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;
//
//        Set<CustomRuleRemark> mappedByGenerator =
//                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;
//
//        Set<CustomRuleRemark> mappedByAnyGenerator =
//                isCustomRuleRemarkExists(DummyCustomGenerator.class) ? getRemarks(DummyCustomGenerator.class) : null;
//
//        if (mappedByAnyGenerator != null) {
//            if (mappedByGenerator == null) {
//                mappedByGenerator = mappedByAnyGenerator;
//            } else {
//                mappedByGenerator.addAll(mappedByAnyGenerator);
//            }
//        }
//
//        if (mappedByField == null && mappedByGenerator == null) {
//            return EMPTY_SET;
//        }
//
        Set<CustomRuleRemark> remarksSet = new HashSet<>();
//
//        if (mappedByGenerator != null) {
//            remarksSet.addAll(mappedByGenerator);
//        }
//        if (mappedByField != null) {
//            remarksSet.addAll(mappedByField);
//        }
        return remarksSet;
    }

//    public Map<CustomRuleRemark, CustomRuleRemarkArgs> getRemarksWithArgs(String fieldName,
//                                                                          Class<?> remarkableGeneratorClass) {
//        Set<CustomRuleRemark> mappedByField =
//                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;
//
//        Set<CustomRuleRemark> mappedByGenerator =
//                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;
//
//        Set<CustomRuleRemark> mappedByAnyGenerator =
//                isCustomRuleRemarkExists(DummyCustomGenerator.class) ? getRemarks(DummyCustomGenerator.class) : null;
//
//        if (mappedByAnyGenerator != null) {
//            if (mappedByGenerator == null) {
//                mappedByGenerator = mappedByAnyGenerator;
//            } else {
//                mappedByGenerator.addAll(mappedByAnyGenerator);
//            }
//        }
//
//        if (mappedByField == null && mappedByGenerator == null) {
//            return EMPTY_MAP;
//        }
//
//        Map<CustomRuleRemark, CustomRuleRemarkArgs> remarksMap = new HashMap<>();
//
//        if (mappedByGenerator != null) {
//            addToMap(remarksMap, mappedByGenerator);
//        }
//        if (mappedByField != null) {
//            addToMap(remarksMap, mappedByField);
//        }
//        return remarksMap;
//    }

    public Map<String, String> getConfigMap(String fieldName,
                                            Class<?> remarkableGeneratorClass) {
        Map<String, String> mappedByField =
                isCustomRuleRemarkExists(fieldName) ? getRemarks(fieldName) : null;

        Map<String, String> mappedByGenerator =
                isCustomRuleRemarkExists(remarkableGeneratorClass) ? getRemarks(remarkableGeneratorClass) : null;

        Map<String, String> mappedByAnyGenerator =
                isCustomRuleRemarkExists(DummyCustomGenerator.class) ? getRemarks(DummyCustomGenerator.class) : null;

        if (mappedByAnyGenerator != null) {
            if (mappedByGenerator == null) {
                mappedByGenerator = mappedByAnyGenerator;
            } else {
                mappedByGenerator.putAll(mappedByAnyGenerator);
            }
        }

        if (mappedByField == null && mappedByGenerator == null) {
            return EMPTY_MAP;
        }

        Map<String, String> remarksMap = new HashMap<>();

        if (mappedByGenerator != null) {
            remarksMap.putAll(mappedByGenerator);
        }
        if (mappedByField != null) {
            remarksMap.putAll(mappedByField);
        }
        return remarksMap;
    }

    /*
     * Private utils
     */

    private Map<String, String> getRemarks(Class<?> customGeneratorClass) {
        return customRuleRemarksMapByGenerator_.get(customGeneratorClass);
    }

    private Map<String, String> getRemarks(String fieldName) {
        return customRuleRemarksMapByField_.get(fieldName);
    }

    private boolean isCustomRuleRemarkExists(Class<?> customGenerator) {
//        return customRuleRemarksMapByGenerator.containsKey(customGenerator);
        return true;
    }

    private boolean isCustomRuleRemarkExists(String fieldName) {
        return customRuleRemarksMapByField_.containsKey(fieldName);
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
