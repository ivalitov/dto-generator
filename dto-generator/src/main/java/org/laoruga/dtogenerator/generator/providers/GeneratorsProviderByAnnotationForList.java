package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorForList;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.suppliers.UserGeneratorSuppliers;
import org.laoruga.dtogenerator.rule.RuleInfoList;

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
    private final UserGeneratorSuppliers userGeneratorSuppliers;


    public GeneratorsProviderByAnnotationForList(GeneratorsProviderByAnnotation generatorsProvider,
                                                 GeneratorConfiguratorForList generatorConfiguratorForList,
                                                 UserGeneratorSuppliers userGeneratorSuppliers) {
        this.generatorsProvider = generatorsProvider;
        this.generatorConfiguratorForList = generatorConfiguratorForList;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
    }

    Generator<?> getGenerator(RuleInfoList listRuleInfo) {

        final Field field = listRuleInfo.getField();
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        Class<?> listElementType = listRuleInfo.getElementType();

        // List generator builder

        Optional<Generator<?>> maybeUserGenerator =
                userGeneratorSuppliers.getGenerator(fieldType);

        if (maybeUserGenerator.isPresent()) {
            return maybeUserGenerator.get();
        }

        Function<ConfigDto, Generator<?>> listGeneratorSupplier = generatorsProvider
                .getDefaultGeneratorSupplier(listRuleInfo.getRule(), fieldType);

        // List element generator builder

        Generator<?> elementGenerator = listRuleInfo.isElementRulesExist() ?
                generatorsProvider.getGenerator(listRuleInfo.getElementRuleInfo()) :
                generatorsProvider.getGeneratorByType(field, listElementType);

        ConfigDto listGeneratorConfig = generatorConfiguratorForList.createGeneratorConfig(
                listRuleInfo,
                elementGenerator,
                fieldType,
                fieldName
        );

        return listGeneratorSupplier.apply(listGeneratorConfig);
    }
}
