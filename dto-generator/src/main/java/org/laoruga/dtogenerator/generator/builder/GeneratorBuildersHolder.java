package org.laoruga.dtogenerator.generator.builder;

import com.google.common.primitives.Primitives;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.*;

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
            foundInfo = buildersInfoMapByGeneratedType.get(Enum.class);
        }

        if (foundInfo == null && Collection.class.isAssignableFrom(generatedType)) {
            foundInfo = buildersInfoMapByGeneratedType.get(Collection.class);
        }

        if (foundInfo == null) {
            return Optional.empty();
        }

        Class<?> buildersGeneratedType = foundInfo.getGeneratedType();

        if (buildersGeneratedType.isAssignableFrom(generatedType)) {
            return Optional.of(foundInfo.getBuilderSupplier().get());
        }

        throw new DtoGeneratorException("Unexpected error. " +
                "Builder's generated type does not match to the field type:" +
                "\n- Builder's generated type: '" + buildersGeneratedType.getName() + "'" +
                "\n- Field type: " + generatedType + "'.");
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
