package org.laoruga.dtogenerator.typegenerators.providers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersDefaultConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.typegenerators.EnumGenerator;
import org.laoruga.dtogenerator.typegenerators.IConfigDto;
import org.laoruga.dtogenerator.typegenerators.builders.GeneratorBuildersHolder;
import org.laoruga.dtogenerator.typegenerators.builders.GeneratorBuildersHolderGeneral;
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
    @Setter
    private Field field;

    public GeneratorBuildersProviderByType(DtoGeneratorInstanceConfig configuration,
                                           GeneratorBuildersHolder userGeneratorBuilders) {
        super(configuration);
        this.userGeneratorBuilders = userGeneratorBuilders;
    }

    @Override
    public Optional<IGenerator<?>> selectOrCreateGenerator() {
        if (DtoGeneratorStaticConfig.getInstance().getGenerateAllKnownTypes()) {
            return selectOrCreateGenerator(getGeneratedType());
        } else {
            return Optional.empty();
        }
    }

    Optional<IGenerator<?>> selectOrCreateGenerator(Class<?> generatedType) {
        Optional<IGeneratorBuilder> maybeBuilder = userGeneratorBuilders.getBuilder(generatedType);
        if (!maybeBuilder.isPresent()) {
            maybeBuilder = generalGeneratorBuilders.getBuilder(generatedType);
        }

        IGenerator<?> generator = null;

        if (maybeBuilder.isPresent()) {
            IGeneratorBuilder genBuilder = maybeBuilder.get();

            if (genBuilder instanceof IGeneratorBuilderConfigurable) {

                BiFunction<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> generatorSupplier;

                if (genBuilder instanceof EnumGenerator.EnumGeneratorBuilder) {

                    generatorSupplier = enumGeneratorSupplier(getGeneratedType());

                } else if (Collection.class.isAssignableFrom(generatedType)) {

                    Class<?> elementType = ReflectionUtils.getSingleGenericType(field);
                    Optional<IGenerator<?>> maybeElementGenerator = selectOrCreateGenerator(elementType);
                    generatorSupplier = collectionGeneratorSupplier(
                            maybeElementGenerator.orElseThrow(
                                    () -> new DtoGeneratorException("Collection element generator not found, for type: " +
                                            "'" + elementType + "'")));
                } else {
                    generatorSupplier = (config, builder) -> builder.build(config, true);
                }

                generator = getGenerator(
                        () -> TypeGeneratorBuildersDefaultConfig.getInstance()
                                .getConfig(genBuilder.getClass(), getGeneratedType()),
                        () -> (IGeneratorBuilderConfigurable) genBuilder,
                        generatorSupplier,
                        getGeneratedType());
            } else {
                log.debug("Unknown generator builder found by field type, trying to build 'as is' without configuring.");
            }
        }

        return Optional.ofNullable(generator);
    }

    private Class<?> getGeneratedType() {
        return field.getType();
    }
}
