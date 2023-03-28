package org.laoruga.dtogenerator.generator.config;

import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.generator.config.dto.ArrayConfigDto;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;
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
    public ConfigDto createGeneratorConfig(RuleInfoCollection arrayRule,
                                           IGenerator<?> elementGenerator,
                                           Class<?> fieldType,
                                           String fieldName) {

        ArrayRule rule = (ArrayRule) arrayRule.getRule();

        Class<?> arrayElementType = ReflectionUtils.getArrayElementType(fieldType);

        ArrayConfigDto configDto = new ArrayConfigDto(rule, arrayElementType);

        return mergeGeneratorConfigurations(
                () -> configDto,
                getArrayGeneratorSpecificConfig(arrayElementType, elementGenerator),
                fieldType,
                fieldName);
    }
}
