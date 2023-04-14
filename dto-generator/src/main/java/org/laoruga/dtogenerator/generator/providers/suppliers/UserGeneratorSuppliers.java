package org.laoruga.dtogenerator.generator.providers.suppliers;

import com.google.common.primitives.Primitives;
import org.laoruga.dtogenerator.api.generators.Generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 06.04.2023
 */
public class UserGeneratorSuppliers {

    private final Map<Class<?>, Supplier<Generator<?>>> generatedTypeGeneratorSupplierMap = new HashMap<>();

    public void setGenerator(Class<?> generatedType, Supplier<Generator<?>> generatorSupplier) {
        generatedTypeGeneratorSupplierMap.put(
                Primitives.wrap(generatedType),
                generatorSupplier
        );
    }

    public Optional<Generator<?>> getGenerator(Class<?> generatedType) {
        generatedType = Primitives.wrap(generatedType);
        Supplier<Generator<?>> generatorSupplier = generatedTypeGeneratorSupplierMap.get(generatedType);
        Generator<?> maybeGenerator = null;
        if (generatorSupplier != null) {
            maybeGenerator = generatorSupplier.get();
        }
        return Optional.ofNullable(maybeGenerator);
    }

}
