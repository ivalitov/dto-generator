package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GeneratorBuildersProvider {

    private final Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders = new HashMap<>();
    private final Map<String, IGeneratorBuilder> overriddenBuildersSpecificFields = new HashMap<>();

    void setGeneratorForFields(String fieldName, IGeneratorBuilder genBuilder) throws DtoGeneratorException {
        if (overriddenBuildersSpecificFields.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for field: '" + fieldName + "'");
        }
        overriddenBuildersSpecificFields.put(fieldName, genBuilder);
    }

    void overrideGenerator(Class<? extends Annotation> rulesClass, @NonNull IGeneratorBuilder genBuilder) {
        if (overriddenBuilders.containsKey(rulesClass)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for Rules: '" + rulesClass + "'");
        }
        overriddenBuilders.put(rulesClass, genBuilder);
    }

    IGenerator<?> getStringGenerator(String fieldName, StringRules stringRules) {
        if (isGeneratorOverridden(fieldName, stringRules)) {
            return getOverriddenGenerator(fieldName, stringRules);
        } else {
            return BasicGeneratorsBuilders.string()
                    .minLength(stringRules.minSymbols())
                    .maxLength(stringRules.maxSymbols())
                    .charset(stringRules.charset())
                    .chars(stringRules.chars())
                    .ruleRemark(stringRules.ruleRemark())
                    .build();
        }
    }

    private boolean isGeneratorOverridden(String fieldName, Annotation rules) {
        return overriddenBuildersSpecificFields.containsKey(fieldName) ||
                overriddenBuilders.containsKey(rules.getClass());
    }

    private IGenerator<?> getOverriddenGenerator(String fieldName, Annotation rules) {
        return overriddenBuildersSpecificFields.getOrDefault(
                fieldName,
                Objects.requireNonNull(overriddenBuilders.get(rules.getClass()))).build();
    }
}
