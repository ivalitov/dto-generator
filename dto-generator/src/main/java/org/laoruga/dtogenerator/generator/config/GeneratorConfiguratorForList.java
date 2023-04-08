package org.laoruga.dtogenerator.generator.config;

import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.generator.config.dto.CollectionConfig;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoList;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyCollectionClass;

import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 28.03.2023
 */
public class GeneratorConfiguratorForList extends GeneratorConfigurator {

    public GeneratorConfiguratorForList(ConfigurationHolder configuration, RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
    }

    @SuppressWarnings("unchecked")
    public ConfigDto createGeneratorConfig(RuleInfoList ruleInfo,
                                           Generator<?> elementGenerator,
                                           Class<?> fieldType,
                                           String fieldName) {

        CollectionRule rule = (CollectionRule) ruleInfo.getRule();

        Class<? extends Collection<?>> collectionClass = rule.collectionClass() == DummyCollectionClass.class
                ? (Class<? extends Collection<?>>) ConcreteClasses.getConcreteCollectionClass((Class<? extends Collection<?>>) fieldType)
                : (Class<? extends Collection<?>>) rule.collectionClass();

        CollectionConfig newConfigInstance = new CollectionConfig(rule)
                .setCollectionInstanceSupplier(() -> ReflectionUtils.createInstance(collectionClass));

        return mergeGeneratorConfigurations(
                () -> newConfigInstance,
                getCollectionGeneratorSpecificConfig(collectionClass, elementGenerator),
                fieldType,
                fieldName);

    }
}
