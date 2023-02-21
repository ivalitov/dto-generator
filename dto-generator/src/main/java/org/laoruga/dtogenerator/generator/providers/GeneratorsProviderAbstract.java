package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.EnumGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.EnumConfigDto;
import org.laoruga.dtogenerator.generator.configs.IConfigDto;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
public abstract class GeneratorsProviderAbstract {

    private final DtoGeneratorInstanceConfig configuration;
    @Getter(AccessLevel.PROTECTED)
    private final RemarksHolder remarksHolder;

    protected GeneratorsProviderAbstract(DtoGeneratorInstanceConfig configuration, RemarksHolder remarksHolder) {
        this.configuration = configuration;
        this.remarksHolder = remarksHolder;
    }

    protected DtoGeneratorInstanceConfig getConfiguration() {
        return configuration;
    }

    public IRuleRemark getRuleRemark(String fieldName) {
        return remarksHolder.getBasicRemarks().isBasicRuleRemarkExists(fieldName) ?
                remarksHolder.getBasicRemarks().getBasicRuleRemark(fieldName) : null;
    }

    protected IGenerator<?> getGenerator(Supplier<IConfigDto> configDtoSupplier,
                                         Supplier<IGeneratorBuilderConfigurable> genBuildSupplier,
                                         BiFunction<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> generatorSupplier,
                                         Class<?> fieldType,
                                         String fieldName) {
        IGeneratorBuilderConfigurable genBuilder = genBuildSupplier.get();


        IConfigDto instanceConfig = getConfiguration().getGeneratorsConfig()
                .getOrNull(genBuilder.getClass(), fieldType);
        IConfigDto staticConfig = DtoGeneratorStaticConfig.getInstance().getGeneratorsConfig()
                .getOrNull(genBuilder.getClass(), fieldType);
        IConfigDto config = configDtoSupplier.get();

        if (staticConfig != null) {
            config.merge(staticConfig);
        }

        if (instanceConfig != null) {
            config.merge(instanceConfig);
        }

        if (getRuleRemark(fieldName) != null) {
            config.setRuleRemark(getRuleRemark(fieldName));
        }

        return generatorSupplier.apply(config, genBuilder);
    }

    protected BiFunction<
            IConfigDto,
            IGeneratorBuilderConfigurable,
            IGenerator<?>> enumGeneratorSupplier(Class<?> generatedType) {
        return (config, builder) -> {
            EnumConfigDto enumConfig = (EnumConfigDto) config;
            if (enumConfig.getEnumClass() == null) {
                if (generatedType.isEnum()) {
                    enumConfig.setEnumClass((Class<? extends Enum<?>>) generatedType);
                } else {
                    throw new DtoGeneratorException("Field type must be enum : '"
                            + generatedType + "'");
                }
            }
            return ((EnumGeneratorBuilder) builder).build(config, true);
        };
    }

    protected BiFunction<IConfigDto, IGeneratorBuilderConfigurable,
            IGenerator<?>> collectionGeneratorSupplier(IGenerator<?> elementGenerator) {
        return (config, builder) -> {
            ((CollectionConfigDto) config).setElementGenerator((IGenerator<Object>) elementGenerator);
            return builder.build(config, true);
        };
    }
}
