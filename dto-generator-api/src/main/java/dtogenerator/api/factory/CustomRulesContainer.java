package dtogenerator.api.factory;

import dtogenerator.api.markup.IRulesDependentCustomGenerator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class CustomRulesContainer {

    private static final CustomRulesContainer INSTANCE = new CustomRulesContainer();

    public static CustomRulesContainer getInstance() {
        return INSTANCE;
    }

    private final Map<Class<? extends Annotation>, Class<? extends IRulesDependentCustomGenerator<?, ? extends Annotation>>>
            customGeneratorsMap = new HashMap<>();

    public void registerCustomGenerator(Class<? extends Annotation> generatorMarker,
                                        Class<? extends IRulesDependentCustomGenerator<?, ? extends Annotation>> customGenerator) {
        if (!customGeneratorsMap.containsKey(generatorMarker)) {
            customGeneratorsMap.putIfAbsent(generatorMarker, customGenerator);
        } else {
            throw new RuntimeException();
        }
    }

    Class<? extends IRulesDependentCustomGenerator<?, ? extends Annotation>> getCustomGenerator(Class<? extends Annotation> generator) {
        if (customGeneratorsMap.containsKey(generator)) {
            return customGeneratorsMap.get(generator);
        } else {
            throw new RuntimeException();
        }
    }

    boolean isCustomGeneratorExists(Class<? extends Annotation> generator) {
        return customGeneratorsMap.containsKey(generator);
    }
}
