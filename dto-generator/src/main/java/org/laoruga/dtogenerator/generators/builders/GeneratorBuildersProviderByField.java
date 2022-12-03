package org.laoruga.dtogenerator.generators.builders;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersDefaultConfig;
import org.laoruga.dtogenerator.generators.basictypegenerators.EnumGenerator;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorBuildersProviderByField extends AbstractGeneratorBuildersProvider {

    private final Map<String, IGeneratorBuilder> overriddenBuildersForFields;
    @Setter
    private Field field;

    public GeneratorBuildersProviderByField(DtoGeneratorInstanceConfig configuration,
                                            Map<String, IGeneratorBuilder> overriddenBuildersForFields) {
        super(configuration);
        this.overriddenBuildersForFields = overriddenBuildersForFields;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<IGenerator<?>> selectOrCreateGenerator() {
        IGenerator<?> generator = null;

        if (overriddenBuildersForFields.containsKey(getFieldName())) {
            IGeneratorBuilder genBuilder = overriddenBuildersForFields.get(getFieldName());

            if (genBuilder instanceof IGeneratorBuilderConfigurable) {

                IGeneratorBuilderConfigurable genBuilderConfigurable = (IGeneratorBuilderConfigurable) genBuilder;

                generator = getGenerator(
                                () -> TypeGeneratorBuildersDefaultConfig.getInstance().getConfig(genBuilderConfigurable.getClass(), getGeneratedType()),
                                () -> genBuilderConfigurable,
                                (config, builder) -> {

                                    if (config instanceof EnumGenerator.ConfigDto) {
                                        ((EnumGenerator.ConfigDto) config).setEnumClass((Class<? extends Enum<?>>) getGeneratedType());
                                    }

                                    return builder.build(config, true);
                                });
            } else {
                log.debug("GenBuilder explicitly set fo field builds as is.");

                generator = genBuilder.build();
            }

        }
        return Optional.ofNullable(generator);
    }

    private String getFieldName() {
        return field.getName();
    }

    private Class<?> getGeneratedType() {
        return field.getType();
    }

}
