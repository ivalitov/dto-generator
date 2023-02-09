package org.laoruga.dtogenerator.generators.providers;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersDefaultConfig;
import org.laoruga.dtogenerator.generators.EnumGenerator;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorBuildersProviderByField extends AbstractGeneratorBuildersProvider {

    private final Map<String, IGeneratorBuilder> overriddenBuildersForFields;

    public GeneratorBuildersProviderByField(DtoGeneratorInstanceConfig configuration,
                                            Map<String, IGeneratorBuilder> overriddenBuildersForFields,
                                            RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.overriddenBuildersForFields = overriddenBuildersForFields;
    }

    public IGenerator<?> getGenerator(Field field) {

        IGeneratorBuilder genBuilder = overriddenBuildersForFields.get(field.getName());

        if (genBuilder instanceof IGeneratorBuilderConfigurable) {

            IGeneratorBuilderConfigurable genBuilderConfigurable = (IGeneratorBuilderConfigurable) genBuilder;

            return getGenerator(
                    () -> TypeGeneratorBuildersDefaultConfig.getInstance()
                            .getConfig(genBuilderConfigurable.getClass(), field.getType()),
                    () -> genBuilderConfigurable,
                    (config, builder) -> {

                        if (config instanceof EnumGenerator.ConfigDto) {
                            ((EnumGenerator.ConfigDto) config).setEnumClass((Class<? extends Enum<?>>) field.getType());
                        }
                        return builder.build(config, true);
                    },
                    field.getType(),
                    field.getName());
        }

        log.debug("Because genBuilder was set explicitly fo field, it builds as is.");
        return genBuilder.build();
    }

}
