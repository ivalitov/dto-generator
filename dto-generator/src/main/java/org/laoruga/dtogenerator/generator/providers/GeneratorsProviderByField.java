package org.laoruga.dtogenerator.generator.providers;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByField  {

    private final Map<String, IGenerator<?>> overriddenGeneratorsForFields;

    public GeneratorsProviderByField() {
        this.overriddenGeneratorsForFields = new HashMap<>();
    }

    synchronized IGenerator<?> getGenerator(Field field) {
       return overriddenGeneratorsForFields.get(field.getName());
    }

    synchronized boolean isGeneratorOverridden(String fieldName) {
        return overriddenGeneratorsForFields.containsKey(fieldName);
    }

    synchronized void setGeneratorBuilderForField(String fieldName, IGenerator<?> generator) {
        if (overriddenGeneratorsForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException(
                    "Generator has already been added explicitly for the field: '" + fieldName + "'");
        }
        overriddenGeneratorsForFields.put(fieldName, generator);
    }
}
