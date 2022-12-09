package org.laoruga.dtogenerator.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.rules.RulesInstance;
import org.laoruga.dtogenerator.typegenerators.*;

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
        setConfig(StringGenerator.StringGeneratorBuilder.class, () -> new StringGenerator.ConfigDto(RulesInstance.stringRule));
        setConfig(IntegerGenerator.IntegerGeneratorBuilder.class, () -> new IntegerGenerator.ConfigDto(RulesInstance.integerRule));
        setConfig(LongGenerator.LongGeneratorBuilder.class, () -> new LongGenerator.ConfigDto(RulesInstance.longRule));
        setConfig(DoubleGenerator.DoubleGeneratorBuilder.class, () -> new DoubleGenerator.ConfigDto(RulesInstance.doubleRule));
        setConfig(LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder.class, () -> new LocalDateTimeGenerator.ConfigDto(RulesInstance.localDateTimeRule));
        setConfig(EnumGenerator.EnumGeneratorBuilder.class, () -> new EnumGenerator.ConfigDto(RulesInstance.enumRule));

        setCollectionConfig(RulesInstance.listRule.generatedType(), () -> new CollectionGenerator.ConfigDto(RulesInstance.listRule));
        setCollectionConfig(RulesInstance.setRule.generatedType(), () -> new CollectionGenerator.ConfigDto(RulesInstance.setRule));
    }

    @Override
    public IConfigDto getConfig(Class<?> builderClass) {
        return Objects.requireNonNull(super.getConfig(builderClass), "Default config not set for builder's class: " +
                "'" + builderClass + "'");
    }

    @Override
    public IConfigDto getConfig(Class<?> builderClass, Class<?> generatedType) {
        return Objects.requireNonNull(super.getConfig(builderClass, generatedType),
                "Default config not set for builder's class: " +
                        "'" + builderClass + "' and field type: '" + generatedType + "'");
    }

    @Override
    public void setConfig(IConfigDto configDto) {
        super.setConfig(configDto);
        logWarning();
    }

    @Override
    public void setCollectionConfig(Class<?> superTypeClass, IConfigDto configDto) {
        super.setCollectionConfig(superTypeClass, configDto);
        logWarning();
    }

    private static void logWarning() {
        log.warn("Default type generator's config have changed, this may conclude to unexpected behaviour.");
    }

}
