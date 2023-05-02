package org.laoruga.dtogenerator.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.CustomGeneratorsConfigMapHolder;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 05.04.2023
 */
public class CustomGeneratorsConfigurationHolder {

    private final Supplier<?> dtoInstanceSupplier;
    private final RemarksHolder remarksHolder;
    private final CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder;
    private final Map<String, CustomGeneratorConfigurator.Builder> byFieldName;
    @Getter(AccessLevel.PUBLIC)
    private final Map<Class<? extends CustomGenerator<?>>, CustomGeneratorConfigurator.Builder> byGeneratorType;

    public CustomGeneratorsConfigurationHolder(Supplier<?> dtoInstanceSupplier,
                                               RemarksHolder remarksHolder,
                                               CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder) {
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        this.remarksHolder = remarksHolder;
        this.customGeneratorsConfigMapHolder = customGeneratorsConfigMapHolder;
        this.byFieldName = new HashMap<>();
        this.byGeneratorType = new HashMap<>();
    }

    /*
     * Constructor to copy
     */
    public CustomGeneratorsConfigurationHolder(Supplier<?> dtoInstanceSupplier,
                                               RemarksHolder remarksHolder,
                                               CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder,
                                               Map<Class<? extends CustomGenerator<?>>, CustomGeneratorConfigurator.Builder> byGeneratorType) {
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        this.remarksHolder = remarksHolder;
        this.customGeneratorsConfigMapHolder = customGeneratorsConfigMapHolder;
        this.byGeneratorType = byGeneratorType;
        this.byFieldName = new HashMap<>();
    }

    public synchronized void setConfiguratorBuilder(String fieldName,
                                                    CustomGeneratorConfigurator.Builder configuratorBuilder) {
        if (byFieldName.containsKey(fieldName)) {
            throw new DtoGeneratorException("Custom generator configurator already set for the field: " +
                    "'" + fieldName + "'");
        }
        byFieldName.put(fieldName, configuratorBuilder);
    }

    public synchronized void setConfiguratorBuilder(Class<? extends CustomGenerator<?>> customGeneratorClass,
                                                    CustomGeneratorConfigurator.Builder configuratorBuilder) {
        if (byGeneratorType.containsKey(customGeneratorClass)) {
            throw new DtoGeneratorException("Custom generator configurator already set for generator: " +
                    "'" + customGeneratorClass + "'");
        }
        byGeneratorType.put(customGeneratorClass, configuratorBuilder);
    }

    public Optional<CustomGeneratorConfigurator.Builder> getBuilder(Class<? extends CustomGenerator<?>> generatorClass) {
        return Optional.ofNullable(byGeneratorType.getOrDefault(generatorClass, null));

    }

    public Optional<CustomGeneratorConfigurator.Builder> getBuilder(String fieldName) {
        return Optional.ofNullable(byFieldName.getOrDefault(fieldName, null));
    }

    public CustomGeneratorConfigurator.Builder getBuilder(String fieldName,
                                                          Class<? extends CustomGenerator<?>> generatorClass,
                                                          String[] args,
                                                          String[] keyValueParams) {
        CustomGeneratorConfigurator.Builder newBuilder = newDefaultBuilder(fieldName)
                .args(args)
                .keyValueParams(keyValueParams);
        getBuilder(generatorClass).ifPresent(newBuilder::merge);
        getBuilder(fieldName).ifPresent(newBuilder::merge);
        return newBuilder;
    }

    public CustomGeneratorConfigurator.Builder getBuilder(String fieldName,
                                                          Class<? extends CustomGenerator<?>> generatorClass) {
        CustomGeneratorConfigurator.Builder newBuilder = newDefaultBuilder(fieldName);
        getBuilder(generatorClass).ifPresent(newBuilder::merge);
        getBuilder(fieldName).ifPresent(newBuilder::merge);
        return newBuilder;
    }

    private static final String[] EMPTY_ARRAY = {};

    public CustomGeneratorConfigurator.Builder newDefaultBuilder(String fieldName) {
        return CustomGeneratorConfigurator.builder()
                .fieldName(fieldName)
                .args(EMPTY_ARRAY)
                .dtoInstanceSupplier(dtoInstanceSupplier)
                .remarksHolder(remarksHolder)
                .customGeneratorsConfigMapHolder(customGeneratorsConfigMapHolder);
    }

    public void setArgs(Class<? extends CustomGenerator<?>> generatorClass, String[] argsOrNull) {

        Optional<CustomGeneratorConfigurator.Builder> maybeBuilder = getBuilder(generatorClass);

        CustomGeneratorConfigurator.Builder builder;

        if (maybeBuilder.isPresent()) {
            builder = maybeBuilder.get();
        } else {
            builder = CustomGeneratorConfigurator.builder()
                    .dtoInstanceSupplier(dtoInstanceSupplier)
                    .remarksHolder(remarksHolder);
            setConfiguratorBuilder(generatorClass, builder);
        }

        if (argsOrNull != null) {
            builder.args(argsOrNull);
        }
    }

    public void setArgs(String fieldName, String[] argsOrNull) {

        Optional<CustomGeneratorConfigurator.Builder> maybeBuilder = getBuilder(fieldName);

        CustomGeneratorConfigurator.Builder builder;

        if (maybeBuilder.isPresent()) {
            builder = maybeBuilder.get();
        } else {
            builder = CustomGeneratorConfigurator.builder()
                    .fieldName(fieldName)
                    .dtoInstanceSupplier(dtoInstanceSupplier)
                    .remarksHolder(remarksHolder);
            setConfiguratorBuilder(fieldName, builder);
        }

        if (argsOrNull != null) {
            builder.args(argsOrNull);
        }
    }

}
