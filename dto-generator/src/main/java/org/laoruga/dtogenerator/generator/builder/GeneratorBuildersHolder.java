package org.laoruga.dtogenerator.generator.builder;

import com.google.common.primitives.Primitives;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.EnumRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.GeneratedTypes;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An instance contain all the possible to use generator providers.
 * It gives methods to retrieving generators by generated type or @Rules.
 *
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */
public final class GeneratorBuildersHolder {

    private final Map<Class<?>, GeneratorBuilderInfo> buildersInfoMap;
    private final Map<Class<?>, GeneratorBuilderInfo> buildersInfoMapByGeneratedType;

    public GeneratorBuildersHolder() {
        this.buildersInfoMap = new HashMap<>();
        this.buildersInfoMapByGeneratedType = new HashMap<>();
    }
    /**
     * Get builder by generated type
     *
     * @param generatedType - type supposed to be generated
     * @return - builder if exists
     */
    public Optional<IGeneratorBuilder<?>> getBuilder(Class<?> generatedType) {

        generatedType = generatedType.isPrimitive() ? Primitives.wrap(generatedType) : generatedType;

        GeneratorBuilderInfo foundInfo = buildersInfoMapByGeneratedType.get(generatedType);

        if (foundInfo == null && generatedType.isEnum()) {
            foundInfo = buildersInfoMapByGeneratedType.get(EnumRule.GENERATED_TYPE);
        }

        if (foundInfo == null && CollectionRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {
            foundInfo = buildersInfoMapByGeneratedType.get(CollectionRule.GENERATED_TYPE);
        }

        if (foundInfo == null && GeneratedTypes.isAssignableFrom(ArrayRule.GENERATED_TYPES, generatedType)) {
            foundInfo = buildersInfoMapByGeneratedType.get(Object[].class);
        }

        if (foundInfo == null && DateTimeRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {
            foundInfo = buildersInfoMapByGeneratedType.get(DateTimeRule.GENERATED_TYPE);
        }

        if (foundInfo == null && MapRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {
            foundInfo = buildersInfoMapByGeneratedType.get(MapRule.GENERATED_TYPE);
        }

        if (foundInfo == null) {
            return Optional.empty();
        }

        return Optional.of(foundInfo.getBuilderSupplier().get());
    }

    public Optional<IGeneratorBuilder<?>> getBuilder(Annotation rulesAnnotation) {
        return Optional.ofNullable(buildersInfoMap.get(rulesAnnotation.annotationType()))
                .map(i -> i.getBuilderSupplier().get());
    }

    public void addBuilder(GeneratorBuilderInfo info) {
        if (info.getGeneratedType() == Object.class) {
            buildersInfoMap.put(info.getRules(), info);
        } else {
            if (buildersInfoMapByGeneratedType.containsKey(info.getGeneratedType())) {
                throw new IllegalArgumentException(
                        "Generator for next type already exists: '" + info.getGeneratedType() + "'");
            }
            buildersInfoMapByGeneratedType.put(info.getGeneratedType(), info);
        }

    }

    void addBuilders(List<GeneratorBuilderInfo> infoList) {
        for (GeneratorBuilderInfo info : infoList) {
            if (info.getGeneratedType() == Object.class) {
                buildersInfoMap.put(info.getRules(), info);
            } else {
                if (buildersInfoMapByGeneratedType.containsKey(info.getGeneratedType())) {
                    throw new IllegalArgumentException(
                            "Generator for next type already exists: '" + info.getGeneratedType() + "'");
                }
                buildersInfoMapByGeneratedType.put(info.getGeneratedType(), info);
            }
        }
    }

    public void addBuilder(Class<?> generatedType, IGeneratorBuilder<?> genBuilder) {
        GeneratorBuilderInfo info = GeneratorBuilderInfo.createInstance(
                null, generatedType, () -> genBuilder
        );
        addBuilder(info);
    }
}
