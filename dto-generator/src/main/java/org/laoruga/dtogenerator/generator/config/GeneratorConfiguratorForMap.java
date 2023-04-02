package org.laoruga.dtogenerator.generator.config;

import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.config.dto.MapConfig;
import org.laoruga.dtogenerator.rule.RuleInfoMap;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyMapClass;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 28.03.2023
 */
public class GeneratorConfiguratorForMap extends GeneratorConfigurator {

    public GeneratorConfiguratorForMap(ConfigurationHolder configuration, RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
    }

    @SuppressWarnings("unchecked")
    public ConfigDto createGeneratorConfig(RuleInfoMap mapRruleInfo,
                                           Generator<?> keyGenerator,
                                           Generator<?> valueGenerator,
                                           Class<?> fieldType,
                                           String fieldName) {

        MapRule rule = mapRruleInfo.getRule();

        Class<? extends Map<?, ?>> mapClass = rule.mapClass() == DummyMapClass.class
                ? (Class<? extends Map<?, ?>>) ConcreteClasses.getConcreteMapClass((Class<? extends Map<?, ?>>) fieldType)
                : (Class<? extends Map<?, ?>>) rule.mapClass();

        MapConfig newConfigInstance = new MapConfig(rule)
                .setMapInstanceSupplier(() -> (Map<Object, Object>) ReflectionUtils.createInstance(mapClass));

        return mergeGeneratorConfigurations(
                () -> newConfigInstance,
                getMapGeneratorSpecificConfig(mapClass, keyGenerator, valueGenerator),
                fieldType,
                fieldName);
    }
}
