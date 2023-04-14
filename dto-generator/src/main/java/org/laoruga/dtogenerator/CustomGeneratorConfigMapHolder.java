package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.util.dummy.DummyCustomGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class CustomGeneratorConfigMapHolder {

    private final Map<String, Map<String, String>> customRuleRemarksMapByField;
    private final Map<Class<? extends CustomGenerator<?>>, Map<String, String>> customRuleRemarksMapByGenerator;

    public CustomGeneratorConfigMapHolder() {
        this(new HashMap<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    CustomGeneratorConfigMapHolder(CustomGeneratorConfigMapHolder toCopy) {
        this(
                toCopy.customRuleRemarksMapByGenerator.entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> new HashMap<>(entry.getValue())
                                )
                        )
        );

    }

    private CustomGeneratorConfigMapHolder(
            Map<Class<? extends CustomGenerator<?>>, Map<String, String>> customRuleRemarksMapByGenerator) {
        this.customRuleRemarksMapByField = new HashMap<>();
        this.customRuleRemarksMapByGenerator = customRuleRemarksMapByGenerator;
    }

    /*
     * Getters and adders
     */

    void addParameterForField(@NonNull String filedName,
                              @NonNull String remarkName,
                              @NonNull String remarkValue) {
        customRuleRemarksMapByField.putIfAbsent(filedName, new HashMap<>());
        customRuleRemarksMapByField.get(filedName).put(remarkName, remarkValue);
    }

    void addParameterForGeneratorType(@NonNull Class<? extends CustomGenerator<?>> generatorClass,
                                      @NonNull String key,
                                      @NonNull String value) {
        customRuleRemarksMapByGenerator.putIfAbsent(generatorClass, new HashMap<>());
        customRuleRemarksMapByGenerator.get(generatorClass).put(key, value);
    }

    public Map<String, String> getConfigMap(String fieldName,
                                            Class<?> generatorClass) {

        Map<String, String> resultConfigMap = new HashMap<>();

        getConfigMap(DummyCustomGenerator.class).ifPresent(resultConfigMap::putAll);
        getConfigMap(generatorClass).ifPresent(resultConfigMap::putAll);
        getConfigMap(fieldName).ifPresent(resultConfigMap::putAll);

        return resultConfigMap;
    }

    /*
     * Private utils
     */

    private Optional<Map<String, String>> getConfigMap(Class<?> customGeneratorClass) {
        return Optional.ofNullable(
                customRuleRemarksMapByGenerator.get(customGeneratorClass)
        );
    }

    private Optional<Map<String, String>> getConfigMap(String fieldName) {
        return Optional.ofNullable(
                customRuleRemarksMapByField.get(fieldName)
        );
    }

}
