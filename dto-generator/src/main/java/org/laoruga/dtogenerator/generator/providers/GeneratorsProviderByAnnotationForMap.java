package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForMap;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfo;
import org.laoruga.dtogenerator.rule.RuleInfoMap;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;


/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForMap {

    private final GeneratorsProviderByAnnotation generatorsProvider;
    private final GeneratorConfiguratorForMap configuratorForMap;

    public GeneratorsProviderByAnnotationForMap(GeneratorsProviderByAnnotation generatorsProvider,
                                                GeneratorConfiguratorForMap configuratorForMap) {
        this.generatorsProvider = generatorsProvider;
        this.configuratorForMap = configuratorForMap;
    }

    Generator<?> getGenerator(RuleInfoMap mapRruleInfo) {

        final Field field = mapRruleInfo.getField();
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        // Map generator builder

        Optional<Function<ConfigDto, Generator<?>>> maybeUsersMapGenBuilder =
                generatorsProvider.getUserGeneratorSupplier(fieldType);

        if (maybeUsersMapGenBuilder.isPresent()) {
            // user generators are not configurable yet
            return maybeUsersMapGenBuilder.get().apply(null);
        }

        // Map key generator builder

        RuleInfo keyRule = mapRruleInfo.getKeyRule();
        Generator<?> keyGenerator = mapRruleInfo.isKeyRulesExist() ?
                generatorsProvider.getGenerator(keyRule) :
                generatorsProvider.getGeneratorByType(field, keyRule.getRequiredType());

        // Map value generator builder

        RuleInfo valueRule = mapRruleInfo.getValueRule();
        Generator<?> valueGenerator = mapRruleInfo.isValueRulesExist() ?
                generatorsProvider.getGenerator(valueRule) :
                generatorsProvider.getGeneratorByType(field, valueRule.getRequiredType());

        Function<ConfigDto, Generator<?>> mapGenBuilder =
                generatorsProvider.getDefaultGeneratorSupplier(
                        mapRruleInfo.getRule(),
                        fieldType
                );

        ConfigDto configDto = configuratorForMap.createGeneratorConfig(
                mapRruleInfo,
                keyGenerator,
                valueGenerator,
                fieldType,
                fieldName
        );

        return mapGenBuilder.apply(configDto);
    }

}
