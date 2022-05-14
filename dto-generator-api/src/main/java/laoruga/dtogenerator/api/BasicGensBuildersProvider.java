package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGenerators;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.rules.StringRules;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class BasicGensBuildersProvider {

    Map<Class<? extends Annotation>, Object> defaultBuilders = new HashMap<>();
    Map<Class<? extends Annotation>, ? extends IGeneratorFactory<?, ?>> overriddenBuilders = new HashMap<>();

    {
        defaultBuilders.put(StringRules.class, BasicGenerators.stringGenerator());
    }

    IGenerator<?> getOverriddenGeneratorOrNull(Class<? extends Annotation> rulesClass) {
        return overriddenBuilders.containsKey(rulesClass) ? overriddenBuilders.get(rulesClass).create() : null;
    }

    IGenerator<?> getStringIGenerator(StringRules stringRules) {
        IGenerator<?> generator = getOverriddenGeneratorOrNull(stringRules.getClass());
        if (generator == null) {
            generator = BasicGenerators.stringGenerator()
                    .minLength(stringRules.minSymbols())
                    .maxLength(stringRules.maxSymbols())
                    .charset(stringRules.charset())
                    .chars(stringRules.chars())
                    .ruleRemark(stringRules.ruleRemark())
                    .build();
        }
        return generator;
    }

    interface IGeneratorFactory<T extends IGenerator<V>, V> {
        IGenerator<V> create();
    }

}
