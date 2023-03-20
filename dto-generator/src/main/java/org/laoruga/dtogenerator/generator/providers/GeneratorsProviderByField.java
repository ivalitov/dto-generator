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
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.EnumConfigDto;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByField extends GeneratorsProviderAbstract {

    private final Map<String, IGeneratorBuilder<?>> overriddenBuildersForFields;

    public GeneratorsProviderByField(ConfigurationHolder configuration,
                                     RemarksHolder remarksHolder) {
        super(configuration, remarksHolder);
        this.overriddenBuildersForFields = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    IGenerator<?> getGenerator(Field field) {

        IGeneratorBuilder<?> genBuilder = overriddenBuildersForFields.get(field.getName());

        if (genBuilder instanceof IGeneratorBuilderConfigurable) {

            IGeneratorBuilderConfigurable<?> genBuilderConfigurable = (IGeneratorBuilderConfigurable<?>) genBuilder;

            BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> generatorSupplier =
                    (config, builder) -> {
                        if (config instanceof EnumConfigDto) {
                            if (field.getType().isEnum()) {
                                ((EnumConfigDto) config).setEnumClass((Class<? extends Enum<?>>) field.getType());
                            } else {
                                throw new IllegalStateException("Enum field type expected.");
                            }
                        }
                        return builder.build(config, true);
                    };

            return getGenerator(
                    TypeGeneratorsDefaultConfigSupplier.getDefaultConfigSupplier(
                            Primitives.wrap(field.getType())
                    ),
                    () -> genBuilderConfigurable,
                    generatorSupplier,
                    field.getType(),
                    field.getName());
        }

        log.debug("Because genBuilder was set explicitly fo field, it builds as is.");
        return genBuilder.build();
    }

    public boolean isBuilderOverridden(String fieldName) {
        return overriddenBuildersForFields.containsKey(fieldName);
    }

    public void setGeneratorBuilderForField(String fieldName, IGeneratorBuilder<?> genBuilder) {
        if (overriddenBuildersForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException(
                    "Generator has already been added explicitly for the field: '" + fieldName + "'");
        }
        overriddenBuildersForFields.put(fieldName, genBuilder);
    }
}
