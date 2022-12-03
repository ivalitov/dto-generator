package org.laoruga.dtogenerator.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.generators.RulesInstance;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeGeneratorBuildersDefaultConfig extends TypeGeneratorBuildersConfig {

    @Getter
    private static final TypeGeneratorBuildersDefaultConfig instance = new TypeGeneratorBuildersDefaultConfig();

    {
        setConfig(StringGenerator.StringGeneratorBuilder.class, new StringGenerator.ConfigDto(RulesInstance.stringRule));
        setConfig(IntegerGenerator.IntegerGeneratorBuilder.class, new IntegerGenerator.ConfigDto(RulesInstance.integerRule));
        setConfig(LongGenerator.LongGeneratorBuilder.class, new LongGenerator.ConfigDto(RulesInstance.longRule));
        setConfig(DoubleGenerator.DoubleGeneratorBuilder.class, new DoubleGenerator.ConfigDto(RulesInstance.doubleRule));
        setConfig(LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder.class, new LocalDateTimeGenerator.ConfigDto(RulesInstance.localDateTimeRule));
        setConfig(EnumGenerator.EnumGeneratorBuilder.class, new EnumGenerator.ConfigDto(RulesInstance.enumRule));
        setConfig(CollectionGenerator.CollectionGeneratorBuilder.class, List.class, new CollectionGenerator.ConfigDto(RulesInstance.listRule));
        setConfig(CollectionGenerator.CollectionGeneratorBuilder.class, Set.class, new CollectionGenerator.ConfigDto(RulesInstance.setRule));
    }

    @Override
    public IConfigDto getConfig(Class<?> builderClass) {
        return Objects.requireNonNull(super.getConfig(builderClass), "Default config not set for builder's class: " +
                "'" + builderClass + "'");
    }

    @Override
    public IConfigDto getConfig(Class<?> builderClass, Class<?> collectionClass) {
        if (super.getConfig(builderClass) != null) {
            return getConfig(builderClass);
        }
        return Objects.requireNonNull(super.getConfig(builderClass, collectionClass), "Default config not set for collection builder's class: " +
                "'" + builderClass + "'");
    }

}
