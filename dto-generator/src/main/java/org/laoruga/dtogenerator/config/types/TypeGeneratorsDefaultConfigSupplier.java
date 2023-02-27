package org.laoruga.dtogenerator.config.types;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.config.MappingHelper;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@Slf4j
public final class TypeGeneratorsDefaultConfigSupplier {

    public static Supplier<ConfigDto> getDefaultConfigSupplier(Class<?> generatedType) {

        if (!MappingHelper.GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.containsKey(generatedType)) {

            for (Map.Entry<Class<?>, Supplier<ConfigDto>> typeSupplierEntry :
                    MappingHelper.GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.entrySet()) {

                if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                    return typeSupplierEntry.getValue();
                }

            }

            throw new IllegalArgumentException("Unknown type: " + generatedType);
        }

        return MappingHelper.GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.get(generatedType);
    }

}
