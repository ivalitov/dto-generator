package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.MapGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.MapConfigDto;
import org.laoruga.dtogenerator.rule.IRuleInfo;
import org.laoruga.dtogenerator.rule.RuleInfoMap;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyMapClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForMap {

    GeneratorsProviderByAnnotation generatorsProvider;

    public GeneratorsProviderByAnnotationForMap(GeneratorsProviderByAnnotation generatorsProvider) {
        this.generatorsProvider = generatorsProvider;
    }

    IGenerator<?> getGenerator(RuleInfoMap mapRruleInfo,
                               Supplier<?> dtoInstanceSupplier,
                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Field field = mapRruleInfo.getField();
        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        // Map generator builder

        Optional<IGeneratorBuilder<?>> maybeUsersMapGenBuilder =
                generatorsProvider.getUsersGenBuilder(fieldType);

        IGeneratorBuilder<?> mapGenBuilder = maybeUsersMapGenBuilder.isPresent() ?
                maybeUsersMapGenBuilder.get() :
                generatorsProvider.getDefaultGenBuilder(
                        mapRruleInfo.getRule(),
                        fieldType
                );

        Class<?>[] keyAndValueType = ReflectionUtils.getPairedGenericType(field);

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

        return buildMapGenerator(
                mapRruleInfo.getRule(),
                mapGenBuilder,
                keyGenerator,
                valueGenerator,
                fieldType,
                fieldName
        );
    }

    @SuppressWarnings("unchecked")
    private IGenerator<?> buildMapGenerator(Annotation mapRule,
                                            IGeneratorBuilder<?> mapGenBuilder,
                                            IGenerator<?> keyGenerator,
                                            IGenerator<?> valueGenerator,
                                            Class<?> fieldType,
                                            String fieldName) {

        Class<? extends Annotation> rulesClass = mapRule.annotationType();

        if (mapGenBuilder instanceof MapGeneratorBuilder) {

            MapConfigDto configDto;

            if (MapRule.class == rulesClass) {

                MapRule rule = (MapRule) mapRule;

                Class<? extends Map<?, ?>> mapClass = rule.mapClass() == DummyMapClass.class
                        ? (Class<? extends Map<?, ?>>) ConcreteClasses.getConcreteMapClass((Class<? extends Map<?, ?>>) fieldType)
                        : (Class<? extends Map<?, ?>>) rule.mapClass();

                configDto = new MapConfigDto(rule)
                        .setMapInstanceSupplier(() -> (Map<Object, Object>) ReflectionUtils.createInstance(mapClass));

                return generatorsProvider.getGenerator(
                        () -> configDto,
                        () -> (IGeneratorBuilderConfigurable<?>) mapGenBuilder,
                        generatorsProvider.getMapGeneratorSupplier(mapClass, keyGenerator, valueGenerator),
                        fieldType,
                        fieldName);
            } else {
                throw new DtoGeneratorException("Unknown rules annotation class '" + rulesClass + "'");
            }
        }

        log.debug("Unknown map builder builds as is, without Rules annotation params passing.");

        return mapGenBuilder.build();
    }

}
