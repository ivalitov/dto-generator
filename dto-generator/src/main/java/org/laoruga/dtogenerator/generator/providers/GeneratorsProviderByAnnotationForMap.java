package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForMap;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.rule.IRuleInfo;
import org.laoruga.dtogenerator.rule.RuleInfoMap;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


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

    IGenerator<?> getGenerator(RuleInfoMap mapRruleInfo,
                               Supplier<?> dtoInstanceSupplier,
                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Field field = mapRruleInfo.getField();
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        // Map generator builder

        Optional<Function<ConfigDto, IGenerator<?>>> maybeUsersMapGenBuilder =
                generatorsProvider.getUserGeneratorSupplier(fieldType);

        if (maybeUsersMapGenBuilder.isPresent()) {
            // user generators are not configurable yet
            return maybeUsersMapGenBuilder.get().apply(null);
        }

        Function<ConfigDto, IGenerator<?>> mapGenBuilder =
                generatorsProvider.getDefaultGeneratorSupplier(
                        mapRruleInfo.getRule(),
                        fieldType
                );

        // Map key generator builder

        IRuleInfo keyRule = mapRruleInfo.getKeyRule();
        IGenerator<?> keyGenerator = mapRruleInfo.isKeyRulesExist() ?
                generatorsProvider.getGenerator(
                        keyRule,
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier) :
                generatorsProvider.getGeneratorByType(field, keyRule.getRequiredType());

        // Map value generator builder

        IRuleInfo valueRule = mapRruleInfo.getValueRule();
        IGenerator<?> valueGenerator = mapRruleInfo.isValueRulesExist() ?
                generatorsProvider.getGenerator(
                        valueRule,
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier) :
                generatorsProvider.getGeneratorByType(field, valueRule.getRequiredType());

        generatorsProvider.prepareCustomRemarks(keyGenerator, fieldName);
        generatorsProvider.prepareCustomRemarks(valueGenerator, fieldName);

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
