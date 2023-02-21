package org.laoruga.dtogenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.generator.configs.IConfigDto;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@Slf4j
public final class TypeGeneratorDefaultConfigSupplier {

    public static Supplier<IConfigDto> getDefaultConfigSupplier(Class<?> generatedType) {

        if (!MappingHelper.GENERATED_TYPE_TO_DEFAULT_CONFIG_DTO_SUPPLIER.containsKey(generatedType)) {

            for (Map.Entry<Class<?>, Supplier<IConfigDto>> typeSupplierEntry :
                    MappingHelper.GENERATED_TYPE_TO_DEFAULT_CONFIG_DTO_SUPPLIER.entrySet()) {

                if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                    return typeSupplierEntry.getValue();
                }

            }

            throw new IllegalArgumentException("Unknown type: " + generatedType);
        }

        return MappingHelper.GENERATED_TYPE_TO_DEFAULT_CONFIG_DTO_SUPPLIER.get(generatedType);
    }

}
