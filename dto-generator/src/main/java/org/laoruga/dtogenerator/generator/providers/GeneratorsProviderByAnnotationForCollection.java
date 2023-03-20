package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.CollectionGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyCollectionClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstanceOfConcreteClass;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForCollection {

    private final GeneratorsProviderByAnnotation generatorsProvider;

    public GeneratorsProviderByAnnotationForCollection(GeneratorsProviderByAnnotation generatorsProvider) {
        this.generatorsProvider = generatorsProvider;
    }

    IGenerator<?> getCollectionGenerator(Field field,
                                                   RuleInfoCollection collectionRuleInfo,
                                                   Supplier<?> dtoInstanceSupplier,
                                                   Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        Class<?> collectionElementType = ReflectionUtils.getSingleGenericType(field);

        // Collection element generator builder

        IGenerator<?> elementGenerator = collectionRuleInfo.isElementRulesExist() ?
                generatorsProvider.getGenerator(
                        collectionRuleInfo.getElementRule(),
                        collectionElementType,
                        fieldName,
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier) :
                generatorsProvider.getGeneratorByType(field, collectionElementType);

        // Collection generator builder

        Optional<IGeneratorBuilder<?>> maybeCollectionUserGenBuilder =
                generatorsProvider.getUsersGenBuilder(fieldType);

        IGeneratorBuilder<?> collectionGenBuilder = maybeCollectionUserGenBuilder.isPresent() ?
                maybeCollectionUserGenBuilder.get() :
                generatorsProvider.getDefaultGenBuilder(
                        collectionRuleInfo.getRule(),
                        fieldType);

        generatorsProvider.prepareCustomRemarks(elementGenerator, fieldName);

        return buildCollectionGenerator(
                collectionRuleInfo.getRule(),
                collectionGenBuilder,
                elementGenerator,
                fieldType,
                fieldName
        );

    }

    @SuppressWarnings("unchecked")
    private IGenerator<?> buildCollectionGenerator(Annotation collectionRule,
                                                   IGeneratorBuilder<?> collectionGenBuilder,
                                                   IGenerator<?> elementGenerator,
                                                   Class<?> fieldType,
                                                   String fieldName) {
        Class<? extends Annotation> rulesClass = collectionRule.annotationType();

        if (collectionGenBuilder instanceof CollectionGeneratorBuilder) {

            CollectionConfigDto configDto;

            if (CollectionRule.class == rulesClass && Collection.class.isAssignableFrom(fieldType)) {

                CollectionRule rule = (CollectionRule) collectionRule;

                Class<? extends Collection<?>> collectionClass = rule.collectionClass() == DummyCollectionClass.class
                        ? (Class<? extends Collection<?>>) ConcreteClasses.getConcreteCollectionClass((Class<? extends Collection<?>>) fieldType)
                        : (Class<? extends Collection<?>>) rule.collectionClass();

                configDto = new CollectionConfigDto(rule)
                        .setCollectionInstanceSupplier(() -> createInstanceOfConcreteClass(collectionClass));

                return generatorsProvider.getGenerator(
                        () -> configDto,
                        () -> (IGeneratorBuilderConfigurable<?>) collectionGenBuilder,
                        generatorsProvider.getCollectionGeneratorSupplier(collectionClass, elementGenerator),
                        fieldType,
                        fieldName);

            } else {
                throw new DtoGeneratorException("Unknown rules annotation class '" + rulesClass + "'");
            }
        }

        log.debug("Unknown collection builder builds as is, without Rules annotation params passing.");

        return collectionGenBuilder.build();
    }
}
