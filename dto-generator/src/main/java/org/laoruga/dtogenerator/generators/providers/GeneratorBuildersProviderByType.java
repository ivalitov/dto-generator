package org.laoruga.dtogenerator.generators.providers;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersDefaultConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.EnumGenerator;
import org.laoruga.dtogenerator.generators.IConfigDto;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersHolderGeneral;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorBuildersProviderByType extends AbstractGeneratorBuildersProvider {

    private final GeneratorBuildersHolder userGeneratorBuilders;
    private final GeneratorBuildersHolder generalGeneratorBuilders = GeneratorBuildersHolderGeneral.getInstance();

    public  GeneratorBuildersProviderByType(DtoGeneratorInstanceConfig configuration,
                                           GeneratorBuildersHolder userGeneratorBuilders,
                                           RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.userGeneratorBuilders = userGeneratorBuilders;
    }

    public Optional<IGenerator<?>> getGenerator(Field field, Class<?> generatedType) {

        Optional<IGeneratorBuilder> maybeBuilder = getGeneratorBuilder(generatedType);

        if (!maybeBuilder.isPresent()) {
            log.debug("Generator builder not found for field type: " + generatedType);
            return Optional.empty();
        }

        if (maybeBuilder.get() instanceof IGeneratorBuilderConfigurable) {
            return configureGenerator(
                    field,
                    generatedType,
                    (IGeneratorBuilderConfigurable) maybeBuilder.get());
        }

        log.debug("Unknown generator builder found by field type, trying to build 'as is' without configuring.");
        return Optional.of(maybeBuilder.get().build());
    }

    private Optional<IGeneratorBuilder> getGeneratorBuilder(Class<?> generatedType) {
        Optional<IGeneratorBuilder> maybeBuilder = userGeneratorBuilders.getBuilder(generatedType);
        if (!maybeBuilder.isPresent()) {
            maybeBuilder = generalGeneratorBuilders.getBuilder(generatedType);
        }
        return maybeBuilder;
    }

    private Optional<IGenerator<?>> configureGenerator(Field field,
                                                       Class<?> generatedType,
                                                       IGeneratorBuilderConfigurable genBuilder) {
        BiFunction<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> generatorSupplier;

        if (Collection.class.isAssignableFrom(generatedType)) {

            Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
            Optional<IGenerator<?>> maybeElementGenerator = getGenerator(field, elementType);
            generatorSupplier = collectionGeneratorSupplier(
                    maybeElementGenerator.orElseThrow(
                            () -> new DtoGeneratorException("Collection element generator not found, for type: " +
                                    "'" + elementType + "'")));

        } else if (genBuilder instanceof EnumGenerator.EnumGeneratorBuilder) {

            generatorSupplier = enumGeneratorSupplier(generatedType);

        } else {

            generatorSupplier = (config, builder) -> builder.build(config, true);

        }

        return Optional.of(
                getGenerator(
                        () -> TypeGeneratorBuildersDefaultConfig.getInstance()
                                .getConfig(genBuilder.getClass(), generatedType),
                        () -> genBuilder,
                        generatorSupplier,
                        generatedType,
                        field.getName()));
    }

}
