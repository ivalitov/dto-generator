package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.generator.configs.ArrayConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;
import org.laoruga.dtogenerator.util.ReflectionUtils;


/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForArray extends GeneratorsProviderByAnnotationForList {

    public GeneratorsProviderByAnnotationForArray(GeneratorsProviderByAnnotation generatorsProvider) {
        super(generatorsProvider);
    }

    @Override
    protected ConfigDto getGeneratorConfig(RuleInfoCollection arrayRule,
                                           IGenerator<?> elementGenerator,
                                           Class<?> fieldType,
                                           String fieldName) {
        ArrayRule rule = (ArrayRule) arrayRule.getRule();

        Class<?> arrayElementType = ReflectionUtils.getArrayElementType(fieldType);

        ArrayConfigDto configDto = new ArrayConfigDto(rule, arrayElementType);

        return generatorsProvider.mergeGeneratorConfigurations(
                () -> configDto,
                generatorsProvider.getArrayGeneratorSupplier(arrayElementType, elementGenerator),
                fieldType,
                fieldName);
    }
}