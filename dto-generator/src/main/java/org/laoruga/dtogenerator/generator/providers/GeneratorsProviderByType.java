package org.laoruga.dtogenerator.generator.providers;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsDefaultConfigSupplier;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generator.builder.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.generator.builder.builders.EnumGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByType extends GeneratorsProviderAbstract {

    private final GeneratorBuildersHolder userGeneratorBuilders;
    private final GeneratorBuildersHolder generalGeneratorBuilders = GeneratorBuildersHolderGeneral.getInstance();

    public GeneratorsProviderByType(ConfigurationHolder configuration,
                                    GeneratorBuildersHolder userGeneratorBuilders,
                                    RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.userGeneratorBuilders = userGeneratorBuilders;
    }

    public Optional<IGenerator<?>> getGenerator(Field field, Class<?> generatedType) {

        generatedType = Primitives.wrap(generatedType);

        Optional<IGeneratorBuilder<?>> maybeBuilder = getGeneratorBuilder(generatedType);

        if (!maybeBuilder.isPresent()) {
            log.debug("Generator builder not found for field type: " + generatedType);
            return Optional.empty();
        }

        IGeneratorBuilder<?> generatorBuilder = maybeBuilder.get();

        if (generatorBuilder instanceof IGeneratorBuilderConfigurable) {
            return configureGenerator(
                    field,
                    generatedType,
                    (IGeneratorBuilderConfigurable<?>) generatorBuilder);
        }

        log.debug("Unknown generator builder found by field type, trying to build 'as is' without configuring.");
        return Optional.of(generatorBuilder.build());
    }

    private Optional<IGeneratorBuilder<?>> getGeneratorBuilder(Class<?> generatedType) {
        Optional<IGeneratorBuilder<?>> maybeBuilder = userGeneratorBuilders.getBuilder(generatedType);
        if (!maybeBuilder.isPresent()) {
            maybeBuilder = generalGeneratorBuilders.getBuilder(generatedType);
        }
        return maybeBuilder;
    }

    static class Some {
        List<String> some;
    }


    @SuppressWarnings("unchecked")
    private Optional<IGenerator<?>> configureGenerator(Field field,
                                                       Class<?> generatedType,
                                                       IGeneratorBuilderConfigurable<?> genBuilder) {
        BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> generatorSupplier;

        if (Collection.class.isAssignableFrom(generatedType)) {

            Class<? extends Collection<?>> generatedTypeCollection = (Class<? extends Collection<?>>) generatedType;

            Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
            Optional<IGenerator<?>> maybeElementGenerator = getGenerator(field, elementType);
            generatorSupplier = collectionGeneratorSupplier(
                    (Class<? extends Collection<?>>) getConcreteCollectionClass(generatedTypeCollection),
                    maybeElementGenerator.orElseThrow(
                            () -> new DtoGeneratorException("Collection element generator not found, for type: " +
                                    "'" + elementType + "'")));

        } else if (genBuilder instanceof EnumGeneratorBuilder) {

            generatorSupplier = enumGeneratorSupplier(generatedType);

        } else {

            generatorSupplier = (config, builder) -> builder.build(config, true);

        }

        return Optional.of(
                getGenerator(
                        TypeGeneratorsDefaultConfigSupplier.getDefaultConfigSupplier(
                                Primitives.wrap(generatedType)
                        ),
                        () -> genBuilder,
                        generatorSupplier,
                        generatedType,
                        field.getName()));
    }

}
