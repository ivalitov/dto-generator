package laoruga.factory;

import laoruga.markup.ICustomGenerator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class GenerationFactory {

    private static final GenerationFactory INSTANCE = new GenerationFactory();

    public static GenerationFactory getInstance() {
        return INSTANCE;
    }

    private final Map<Class<? extends Annotation>, ICustomGenerator<?, ? extends Annotation>> customGeneratorsMap = new HashMap<>();

//    public void registerCustomGenerator(Class<? extends Annotation> generatorMarker, ICustomGenerator<?, ? extends Annotation> customGenerator) {
    public void registerCustomGenerator(Class<? extends Annotation> generatorMarker, ICustomGenerator<Class<?>, ? extends Annotation> customGenerator) {
        if (!customGeneratorsMap.containsKey(generatorMarker)) {
            customGeneratorsMap.putIfAbsent(generatorMarker, customGenerator);
        } else {
            throw new RuntimeException();
        }
    }

    ICustomGenerator<?, ? extends Annotation> getCustomGenerator(Class<? extends Annotation> generator) {
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
