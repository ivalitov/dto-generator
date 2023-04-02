package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForList;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;


/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForList {

    protected final GeneratorConfiguratorForList generatorConfiguratorForList;
    protected final GeneratorsProviderByAnnotation generatorsProvider;

    public GeneratorsProviderByAnnotationForList(GeneratorsProviderByAnnotation generatorsProvider,
                                                 GeneratorConfiguratorForList generatorConfiguratorForList) {
        this.generatorsProvider = generatorsProvider;
        this.generatorConfiguratorForList = generatorConfiguratorForList;
    }

    IGenerator<?> getGenerator(RuleInfoCollection collectionRuleInfo) {

        final Field field = collectionRuleInfo.getField();
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        Class<?> collectionElementType = collectionRuleInfo.getElementType();

        // Collection element generator builder

        IGenerator<?> elementGenerator = collectionRuleInfo.isElementRulesExist() ?
                generatorsProvider.getGenerator(collectionRuleInfo.getElementRuleInfo()) :
                generatorsProvider.getGeneratorByType(field, collectionElementType);

        // Collection generator builder

        Optional<Function<ConfigDto, IGenerator<?>>> maybeUserCollectionGenerator =
                generatorsProvider.getUserGeneratorSupplier(fieldType);

        if (maybeUserCollectionGenerator.isPresent()) {
            // user generators are not configurable yet
            return maybeUserCollectionGenerator.get().apply(null);
        }

        ConfigDto listGeneratorConfig = generatorConfiguratorForList.createGeneratorConfig(
                collectionRuleInfo,
                elementGenerator,
                fieldType,
                fieldName
        );

        return generatorsProvider
                .getDefaultGeneratorSupplier(collectionRuleInfo.getRule(), fieldType)
                .apply(listGeneratorConfig);
    }
}
