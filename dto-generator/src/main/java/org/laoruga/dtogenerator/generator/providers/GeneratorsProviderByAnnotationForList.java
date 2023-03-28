package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyCollectionClass;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForList {

    protected final GeneratorsProviderByAnnotation generatorsProvider;

    public GeneratorsProviderByAnnotationForList(GeneratorsProviderByAnnotation generatorsProvider) {
        this.generatorsProvider = generatorsProvider;
    }

    IGenerator<?> getGenerator(RuleInfoCollection collectionRuleInfo,
                               Supplier<?> dtoInstanceSupplier,
                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Field field = collectionRuleInfo.getField();
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        Class<?> collectionElementType = collectionRuleInfo.getElementType();

        // Collection element generator builder

        IGenerator<?> elementGenerator = collectionRuleInfo.isElementRulesExist() ?
                generatorsProvider.getGenerator(
                        collectionRuleInfo.getElementRuleInfo(),
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier) :
                generatorsProvider.getGeneratorByType(field, collectionElementType);

        // Collection generator builder

        Optional<Function<ConfigDto, IGenerator<?>>> maybeUserCollectionGenerator =
                generatorsProvider.getUserGeneratorSupplier(fieldType);


        if (maybeUserCollectionGenerator.isPresent()) {
            // user generators are not configurable yet
            return maybeUserCollectionGenerator.get().apply(null);
        } else {
            Function<ConfigDto, IGenerator<?>> defaultGenBuilder = generatorsProvider.getDefaultGenBuilder(
                    collectionRuleInfo.getRule(),
                    fieldType);
            ConfigDto listGeneratorConfig = getGeneratorConfig(
                    collectionRuleInfo,
                    elementGenerator,
                    fieldType,
                    fieldName
            );

            IGenerator<?> generator = defaultGenBuilder.apply(listGeneratorConfig);

            generatorsProvider.prepareCustomRemarks(elementGenerator, fieldName);
            generatorsProvider.prepareCustomRemarks(generator, fieldName);

            return generator;
        }
    }

    @SuppressWarnings("unchecked")
    protected ConfigDto getGeneratorConfig(RuleInfoCollection ruleInfo,
                                           IGenerator<?> elementGenerator,
                                           Class<?> fieldType,
                                           String fieldName) {
        CollectionRule rule = (CollectionRule) ruleInfo.getRule();

        Class<? extends Collection<?>> collectionClass = rule.collectionClass() == DummyCollectionClass.class
                ? (Class<? extends Collection<?>>) ConcreteClasses.getConcreteCollectionClass((Class<? extends Collection<?>>) fieldType)
                : (Class<? extends Collection<?>>) rule.collectionClass();

        CollectionConfigDto configDto = new CollectionConfigDto(rule)
                .setCollectionInstanceSupplier(() -> ReflectionUtils.createInstance(collectionClass));

        return generatorsProvider.mergeGeneratorConfigurations(
                () -> configDto,
                generatorsProvider.getCollectionGeneratorSupplier(collectionClass, elementGenerator),
                fieldType,
                fieldName);

    }
}
