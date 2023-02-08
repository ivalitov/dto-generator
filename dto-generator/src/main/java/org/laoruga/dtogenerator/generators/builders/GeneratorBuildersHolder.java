package org.laoruga.dtogenerator.generators.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An object contains all the possible to use generator providers.
 * It gives methods to retrieving generators by generated type or @Rules.
 *
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */
public final class GeneratorBuildersHolder {

    private final Map<Class<?>, GeneratorBuilderInfo> buildersInfoMap;

    public GeneratorBuildersHolder() {
        this.buildersInfoMap = new HashMap<>();
    }

    /**
     * Get builder by generated type
     *
     * @param generatedType - type supposed to be generated
     * @return - builder if exists
     */
    public Optional<IGeneratorBuilder> getBuilder(Class<?> generatedType) {
        if (generatedType == Object.class) {
            throw new IllegalArgumentException("It is not possible to pick generator for type: '" + generatedType + "'");
        }
        if (generatedType.isEnum()) {
            return getEnumBuilder(generatedType);
        }
        if (buildersInfoMap.containsKey(generatedType)) {
            return Optional.of(buildersInfoMap.get(generatedType).getBuilderSupplier().get());
        }
        return Optional.empty();
    }

    private Optional<IGeneratorBuilder> getEnumBuilder(Class<?> generatedType) {

        if (buildersInfoMap.containsKey(generatedType)) {
            return Optional.of(buildersInfoMap.get(generatedType).getBuilderSupplier().get());
        }

        if (buildersInfoMap.containsKey(Enum.class)) {
            return Optional.of(buildersInfoMap.get(Enum.class).getBuilderSupplier().get());
        }

        return Optional.empty();
    }

    /**
     * Get builder by generated type with check of type matching
     *
     * @param rulesAnnotation - rule for pick generator
     * @param generatedType   - type supposed to be generated
     * @return - builder if exists
     */
    public Optional<IGeneratorBuilder> getBuilder(Annotation rulesAnnotation, Class<?> generatedType) {

        GeneratorBuilderInfo foundInfo = buildersInfoMap.get(rulesAnnotation.annotationType());

        if (foundInfo == null) {
            return Optional.empty();
        }

        Class<?> buildersGeneratedType = foundInfo.getGeneratedType();

        if (buildersGeneratedType.isAssignableFrom(generatedType) ||
                (generatedType.isPrimitive() &&
                        generatedType == foundInfo.getGeneratedTypePrimitive())) {

            return Optional.of(foundInfo.getBuilderSupplier().get());
        }

        throw new DtoGeneratorException("For rules: '" + rulesAnnotation + "'" +
                " builder's generated type: '" + buildersGeneratedType + "'" +
                " not matched to field type: " + generatedType + "'.");
    }

    public void addBuilder(Class<? extends Annotation> rulesClass,
                           Class<?> generatedType,
                           IGeneratorBuilder genBuilder) {
        GeneratorBuilderInfo info = GeneratorBuilderInfo.createInstance(rulesClass, generatedType, () -> genBuilder);
        addBuilder(info);
    }

    public void addBuilder(GeneratorBuilderInfo info) {
        if (info.getGeneratedType() != Object.class) {
            if (buildersInfoMap.containsKey(info.getGeneratedType())) {
                throw new IllegalArgumentException(
                        "Generator for next type already exists: '" + info.getGeneratedType() + "'");
            }
            buildersInfoMap.put(info.getGeneratedType(), info);
        }
        if (buildersInfoMap.containsKey(info.getRules())) {
            throw new IllegalArgumentException(
                    "Generator for next rules annotation already exists: '" + info.getGeneratedType() + "'");
        }
        buildersInfoMap.put(info.getRules(), info);
    }

}
