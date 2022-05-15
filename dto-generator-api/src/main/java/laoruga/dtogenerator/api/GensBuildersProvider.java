package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.rules.StringRules;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class GensBuildersProvider {

    private final Map<String, IGenerator<?>> explicitlyAddedGeneratorForFields = new HashMap<>();
    Map<Class<? extends Annotation>, IGenerator<?>> overriddenBuilders = new HashMap<>();

    void addExplicitlyAddedGeneratorForFields(String fieldName, IGenerator<?> generator) {
        if (explicitlyAddedGeneratorForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for field: '" + fieldName + "'");
        }
        explicitlyAddedGeneratorForFields.put(fieldName, generator);
    }

    void overrideGenerator(Class<? extends Annotation> rulesClass, IGenerator<?> newGenerator) {
        if (overriddenBuilders.containsKey(rulesClass)) {
            throw new DtoGeneratorException("Generator has already been overridden: '" + rulesClass + "'");
        }
        overriddenBuilders.put(rulesClass, newGenerator);
    }

    IGenerator<?> getStringIGenerator(StringRules stringRules) {
        IGenerator<?> generator = getOverriddenGeneratorOrNull(stringRules.getClass());
        if (generator == null) {
            generator = BasicGeneratorsBuilders.stringGenerator()
                    .minLength(stringRules.minSymbols())
                    .maxLength(stringRules.maxSymbols())
                    .charset(stringRules.charset())
                    .chars(stringRules.chars())
                    .ruleRemark(stringRules.ruleRemark())
                    .build();
        }
        return generator;
    }

    private IGenerator<?> getOverriddenGeneratorOrNull(Class<? extends Annotation> rulesClass) {
        return overriddenBuilders.getOrDefault(rulesClass, null);
    }
}
