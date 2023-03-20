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
import org.laoruga.dtogenerator.rule.RuleInfoMap;
import org.laoruga.dtogenerator.util.ConcreteClasses;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyMapClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstanceOfConcreteClassAsObject;

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

    IGenerator<?> getMapGenerator(Field field,
                                         RuleInfoMap mapRruleInfo,
                                         Supplier<?> dtoInstanceSupplier,
                                         Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        final Class<?> fieldType = field.getType();
        final String fieldName = field.getName();

        // Map generator builder

        Optional<IGeneratorBuilder<?>> maybeUsersMapGenBuilder =
                generatorsProvider.getUsersGenBuilder(fieldType);

        boolean isUserMapBuilder = maybeUsersMapGenBuilder.isPresent();

        IGeneratorBuilder<?> mapGenBuilder = isUserMapBuilder ?
                maybeUsersMapGenBuilder.get() :
                generatorsProvider.getDefaultGenBuilder(
                        mapRruleInfo.getRule(),
                        fieldType);

        Class<?>[] keyAndValueType = ReflectionUtils.getPairedGenericType(field);

        // Map key generator builder

        Class<?> keyType = keyAndValueType[0];
        IGenerator<?> keyGenerator = mapRruleInfo.isKeyRulesExist() ?
                generatorsProvider.getGenerator(mapRruleInfo.getKeyRule(),
                        keyType,
                        fieldName,
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier) :
                generatorsProvider.getGeneratorByType(field, keyType);

        // Map value generator builder

        Class<?> valueType = keyAndValueType[1];
        IGenerator<?> valueGenerator = mapRruleInfo.isValueRulesExist() ?
                generatorsProvider.getGenerator(mapRruleInfo.getValueRule(),
                        valueType,
                        fieldName,
                        dtoInstanceSupplier,
                        nestedDtoGeneratorSupplier) :
                generatorsProvider.getGeneratorByType(field, valueType);

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

            if (MapRule.class == rulesClass && Map.class.isAssignableFrom(fieldType)) {

                MapRule rule = (MapRule) mapRule;

                Class<? extends Map<?, ?>> mapClass = rule.mapClass() == DummyMapClass.class
                        ? (Class<? extends Map<?, ?>>) ConcreteClasses.getConcreteMapClass((Class<? extends Map<?, ?>>) fieldType)
                        : (Class<? extends Map<?, ?>>) rule.mapClass();

                configDto = new MapConfigDto(rule)
                        .setMapInstanceSupplier(() -> (Map<Object, Object>) createInstanceOfConcreteClassAsObject(mapClass));

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
