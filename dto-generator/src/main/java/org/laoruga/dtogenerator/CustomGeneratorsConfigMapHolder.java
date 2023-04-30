package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.util.dummy.DummyCustomGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class CustomGeneratorsConfigMapHolder {

    private final Map<String, Map<String, String>> configMapsByField;
    private final Map<Class<? extends CustomGenerator<?>>, Map<String, String>> configMapsByGenerator;

    public CustomGeneratorsConfigMapHolder() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    CustomGeneratorsConfigMapHolder(CustomGeneratorsConfigMapHolder toCopy) {
        this(
                toCopy.configMapsByGenerator.entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> new ConcurrentHashMap<>(entry.getValue())
                                )
                        )
        );

    }

    private CustomGeneratorsConfigMapHolder(
            Map<Class<? extends CustomGenerator<?>>, Map<String, String>> customRuleRemarksMapByGenerator) {
        this.configMapsByField = new HashMap<>();
        this.configMapsByGenerator = customRuleRemarksMapByGenerator;
    }

    /*
     * Getters and adders
     */

    void addParameterForField(@NonNull String filedName,
                              @NonNull String remarkName,
                              @NonNull String remarkValue) {
        configMapsByField.putIfAbsent(filedName, new HashMap<>());
        configMapsByField.get(filedName).put(remarkName, remarkValue);
    }

    void addParameterForGeneratorType(@NonNull Class<? extends CustomGenerator<?>> generatorClass,
                                      @NonNull String key,
                                      @NonNull String value) {
        configMapsByGenerator.putIfAbsent(generatorClass, new HashMap<>());
        configMapsByGenerator.get(generatorClass).put(key, value);
    }

    public Map<String, String> fillConfigMap(String fieldName,
                                             Class<?> generatorClass,
                                             Map<String, String> configMap) {

        getConfigMap(DummyCustomGenerator.class).ifPresent(configMap::putAll);
        getConfigMap(generatorClass).ifPresent(configMap::putAll);
        getConfigMap(fieldName).ifPresent(configMap::putAll);

        return configMap;
    }

    /*
     * Private utils
     */

    private Optional<Map<String, String>> getConfigMap(Class<?> customGeneratorClass) {
        return Optional.ofNullable(
                configMapsByGenerator.get(customGeneratorClass)
        );
    }

    private Optional<Map<String, String>> getConfigMap(String fieldName) {
        return Optional.ofNullable(
                configMapsByField.get(fieldName)
        );
    }

}
