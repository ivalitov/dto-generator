package org.laoruga.dtogenerator.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.rule.RulesInstance;

import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@Slf4j
public final class TypeGeneratorBuildersDefaultConfig extends TypeGeneratorBuildersConfig {

    @Getter
    private static final TypeGeneratorBuildersDefaultConfig instance = new TypeGeneratorBuildersDefaultConfig();

    private TypeGeneratorBuildersDefaultConfig() {
        setConfig(StringGeneratorBuilder.class, () -> new StringConfigDto(RulesInstance.stringRule));
        setConfig(IntegerGeneratorBuilder.class, () -> new IntegerConfigDto(RulesInstance.integerRule));
        setConfig(LongGeneratorBuilder.class, () -> new LongConfigDto(RulesInstance.longRule));
        setConfig(DoubleGeneratorBuilder.class, () -> new DoubleConfigDto(RulesInstance.doubleRule));
        setConfig(LocalDateTimeGeneratorBuilder.class, () -> new LocalDateTimeConfigDto(RulesInstance.localDateTimeRule));
        setConfig(EnumGeneratorBuilder.class, () -> new EnumConfigDto(RulesInstance.enumRule));

        setCollectionConfig(RulesInstance.listRule.generatedType(), () -> new CollectionConfigDto(RulesInstance.listRule));
        setCollectionConfig(RulesInstance.setRule.generatedType(), () -> new CollectionConfigDto(RulesInstance.setRule));
    }

}
