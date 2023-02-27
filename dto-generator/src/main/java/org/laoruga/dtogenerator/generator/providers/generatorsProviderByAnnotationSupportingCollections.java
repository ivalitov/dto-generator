package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.api.rules.SetRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generator.builder.builders.CollectionGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.rule.IRuleInfo;
import org.laoruga.dtogenerator.rule.RuleInfoCollection;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createCollectionInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class generatorsProviderByAnnotationSupportingCollections extends GeneratorsProviderByAnnotation {

    public generatorsProviderByAnnotationSupportingCollections(ConfigurationHolder configuration,
                                                               GeneratorsProviderByType generatorsProviderByType,
                                                               RemarksHolder remarksHolder,
                                                               GeneratorBuildersHolder userGeneratorBuildersHolder) {
        super(configuration,
                generatorsProviderByType,
                remarksHolder,
                userGeneratorBuildersHolder);
    }

    protected IGenerator<?> getCollectionGenerator(Field field,
                                                 IRuleInfo ruleInfo,
                                                 Supplier<?> dtoInstanceSupplier,
                                                 Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        Class<?> collectionElementType = ReflectionUtils.getSingleGenericType(field);

        RuleInfoCollection collectionRuleInfo = (RuleInfoCollection) ruleInfo;

        // Collection generator builder

        Optional<IGeneratorBuilder> maybeCollectionUserGenBuilder = getUsersGenBuilder(
                collectionRuleInfo.getRule(),
                fieldType);

        boolean isUserCollectionBuilder = maybeCollectionUserGenBuilder.isPresent();

        IGeneratorBuilder collectionGenBuilder = isUserCollectionBuilder ?
                maybeCollectionUserGenBuilder.get() :
                getDefaultGenBuilder(
                        collectionRuleInfo.getRule(),
                        fieldType);

        // Collection element generator builder

        IGenerator<?> elementGenerator;
        if (collectionRuleInfo.isElementRulesExist()) {
            IRuleInfo elementRuleInfo = collectionRuleInfo.getElementRule();

            Optional<IGeneratorBuilder> maybeUsersElementGenBuilder = getUsersGenBuilder(
                    elementRuleInfo.getRule(),
                    collectionElementType);

            boolean isUserBuilder = maybeUsersElementGenBuilder.isPresent();

            IGeneratorBuilder elementGenBuilder = isUserBuilder ?
                    maybeUsersElementGenBuilder.get() :
                    getDefaultGenBuilder(
                            elementRuleInfo.getRule(),
                            collectionElementType);

            elementGenerator = buildGenerator(
                    elementRuleInfo.getRule(),
                    elementGenBuilder,
                    collectionElementType,
                    fieldName,
                    dtoInstanceSupplier,
                    nestedDtoGeneratorSupplier);

        } else {

            Optional<IGenerator<?>> maybeGenerator = getGeneratorsProviderByType()
                    .getGenerator(field, collectionElementType);

            if (!maybeGenerator.isPresent()) {
                throw new DtoGeneratorException("Collection element rules absent on the field," +
                        " and element generator wasn't evaluated by type.");
            }

            elementGenerator = maybeGenerator.get();
        }

        prepareCustomRemarks(elementGenerator, fieldName);

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
                                                   IGeneratorBuilder collectionGenBuilder,
                                                   IGenerator<?> elementGenerator,
                                                   Class<?> fieldType,
                                                   String fieldName) {
        Class<? extends Annotation> rulesClass = collectionRule.annotationType();

        if (collectionGenBuilder instanceof CollectionGeneratorBuilder) {

            CollectionConfigDto configDto;

            if (ListRule.class == rulesClass) {

                configDto = new CollectionConfigDto((ListRule) collectionRule)
                        .setCollectionInstance(
                                () -> createCollectionInstance(((ListRule) collectionRule).listClass()));

            } else if (SetRule.class == rulesClass) {

                configDto = new CollectionConfigDto((SetRule) collectionRule)
                        .setCollectionInstance(
                                () -> createCollectionInstance(((SetRule) collectionRule).setClass()));

            } else {
                throw new DtoGeneratorException("Unknown rules annotation class '" + rulesClass + "'");
            }

            return getGenerator(
                    () -> configDto,
                    () -> (IGeneratorBuilderConfigurable) collectionGenBuilder,
                    collectionGeneratorSupplier(elementGenerator),
                    fieldType,
                    fieldName);
        }

        log.debug("Unknown collection builder builds as is, without Rules annotation params passing.");

        return collectionGenBuilder.build();
    }

}
