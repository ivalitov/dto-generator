package org.laoruga.dtogenerator.generator.config;

import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.generator.config.dto.ArrayConfig;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoList;
import org.laoruga.dtogenerator.util.ReflectionUtils;

/**
 * @author Il'dar Valitov
 * Created on 28.03.2023
 */
public class GeneratorConfiguratorForArray extends GeneratorConfiguratorForList {

    public GeneratorConfiguratorForArray(ConfigurationHolder configuration, RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
    }

    @Override
    public ConfigDto createGeneratorConfig(RuleInfoList arrayRule,
                                           Generator<?> elementGenerator,
                                           Class<?> fieldType,
                                           String fieldName) {

        ArrayRule rule = (ArrayRule) arrayRule.getRule();

        Class<?> arrayElementType = ReflectionUtils.getArrayElementType(fieldType);

        ArrayConfig configDto = new ArrayConfig(rule, arrayElementType);

        return mergeGeneratorConfigurations(
                () -> configDto,
                getArrayGeneratorSpecificConfig(arrayElementType, elementGenerator),
                fieldType,
                fieldName);
    }
}
