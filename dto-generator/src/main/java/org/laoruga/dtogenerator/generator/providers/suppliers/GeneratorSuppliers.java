package org.laoruga.dtogenerator.generator.providers.suppliers;

import com.google.common.primitives.Primitives;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.EnumRule;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * An instance contain all the possible to use generator suppliers.
 * It gives methods to retrieving generator by generated type or @Rules.
 *
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */
public final class GeneratorSuppliers {

    /**
     * Map for rule annotations with 'Object.class' generated type
     */
    private final Map<Class<? extends Annotation>, GeneratorSupplierInfo> rulesClassGeneratorInfoMap;
    private final Map<Class<?>, GeneratorSupplierInfo> generatedTypeGeneratorInfoMap;

    public GeneratorSuppliers() {
        this.rulesClassGeneratorInfoMap = new HashMap<>();
        this.generatedTypeGeneratorInfoMap = new HashMap<>();
    }

    /**
     * Get generator supplier by generated type
     *
     * @param generatedType - type supposed to be generated
     * @return - supplier if exists
     */
    public Optional<Function<ConfigDto, IGenerator<?>>> getGeneratorSupplier(Class<?> generatedType) {

        generatedType = generatedType.isPrimitive() ? Primitives.wrap(generatedType) : generatedType;

        GeneratorSupplierInfo foundInfo = generatedTypeGeneratorInfoMap.get(generatedType);

        if (foundInfo == null) {

            if (generatedType.isEnum()) {

                foundInfo = generatedTypeGeneratorInfoMap.get(EnumRule.GENERATED_TYPE);

            } else if (CollectionRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

                foundInfo = generatedTypeGeneratorInfoMap.get(CollectionRule.GENERATED_TYPE);

            } else if (GeneratedTypes.isAssignableFrom(ArrayRule.GENERATED_TYPES, generatedType)) {

                foundInfo = generatedTypeGeneratorInfoMap.get(
                        GeneratedTypes.getAssignableType(ArrayRule.GENERATED_TYPES, generatedType));

            } else if (DateTimeRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

                foundInfo = generatedTypeGeneratorInfoMap.get(DateTimeRule.GENERATED_TYPE);

            } else if (MapRule.GENERATED_TYPE.isAssignableFrom(generatedType)) {

                foundInfo = generatedTypeGeneratorInfoMap.get(MapRule.GENERATED_TYPE);
            }

            if (foundInfo == null) {
                return Optional.empty();
            }

        }

        return Optional.of(foundInfo.getGeneratorSupplier());
    }

    public Optional<Function<ConfigDto, IGenerator<?>>> getGeneratorSupplier(Annotation rulesAnnotation) {
        return Optional.ofNullable(rulesClassGeneratorInfoMap.get(rulesAnnotation.annotationType()))
                .map(GeneratorSupplierInfo::getGeneratorSupplier);
    }

    public void addSuppliersInfo(GeneratorSupplierInfo info) {
        if (info.getGeneratedType() == Object.class) {

            if (rulesClassGeneratorInfoMap.containsKey(info.getRules())) {
                throw new IllegalArgumentException(
                        "Generator info for next rules already exists: '" + info.getRules() + "'");
            }

            rulesClassGeneratorInfoMap.put(info.getRules(), info);

        } else {

            if (generatedTypeGeneratorInfoMap.containsKey(info.getGeneratedType())) {
                throw new IllegalArgumentException(
                        "Generator info for next type already exists: '" + info.getGeneratedType() + "'");
            }

            generatedTypeGeneratorInfoMap.put(info.getGeneratedType(), info);
        }
    }

    void addSuppliersInfo(List<GeneratorSupplierInfo> infoList) {
        for (GeneratorSupplierInfo info : infoList) {
            addSuppliersInfo(info);
        }
    }

    public void addSuppliersInfo(Class<?> generatedType, IGenerator<?> generator) {
        GeneratorSupplierInfo info = GeneratorSupplierInfo.createInstance(
                null, generatedType, (configDto) -> generator
        );
        addSuppliersInfo(info);
    }
}
