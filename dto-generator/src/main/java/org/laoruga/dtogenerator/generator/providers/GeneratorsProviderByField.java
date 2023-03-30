package org.laoruga.dtogenerator.generator.providers;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.CustomGeneratorConfigurator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByField {

    private final Map<String, IGenerator<?>> overriddenGeneratorsForFields;
    private final Supplier<?> dtoInstanceSupplier;

    public GeneratorsProviderByField(Supplier<?> dtoInstanceSupplier) {
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        this.overriddenGeneratorsForFields = new HashMap<>();
    }

    synchronized IGenerator<?> getGenerator(Field field) {
        return overriddenGeneratorsForFields.get(field.getName());
    }

    synchronized boolean isGeneratorOverridden(String fieldName) {
        return overriddenGeneratorsForFields.containsKey(fieldName);
    }

    synchronized void setGeneratorBuilderForField(String fieldName, IGenerator<?> generator, String... args) {
        if (overriddenGeneratorsForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException(
                    "Generator has already been added explicitly for the field: '" + fieldName + "'");
        }
        if (generator instanceof ICustomGenerator) {
            CustomGeneratorConfigurator.builder()
                    .args(args)
                    .dtoInstanceSupplier(dtoInstanceSupplier)
                    .build()
                    .configure((ICustomGenerator<?>) generator);
        }
        overriddenGeneratorsForFields.put(fieldName, generator);
    }
}
