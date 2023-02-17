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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByField extends GeneratorsProviderAbstract {

    private final Map<String, IGeneratorBuilder> overriddenBuildersForFields;

    public GeneratorsProviderByField(DtoGeneratorInstanceConfig configuration,
                                     RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.overriddenBuildersForFields = new HashMap<>();
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

    public boolean isBuilderOverridden(String fieldName) {
        return overriddenBuildersForFields.containsKey(fieldName);
    }

    public void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder genBuilder) {
        if (overriddenBuildersForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator already has been added explicitly for the field: '" + fieldName + "'");
        }
        overriddenBuildersForFields.put(fieldName, genBuilder);
    }
}
