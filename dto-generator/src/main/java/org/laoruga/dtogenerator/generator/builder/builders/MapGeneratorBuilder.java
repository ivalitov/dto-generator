package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.MapGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.MapConfigDto;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public class MapGeneratorBuilder implements IGeneratorBuilderConfigurable<Map<Object, Object>> {
    protected final MapConfigDto configDto;

    public MapGeneratorBuilder() {
        this.configDto = new MapConfigDto();
    }

    public MapGeneratorBuilder minSize(int minSize) {
        configDto.setMinSize(minSize);
        return this;
    }

    public MapGeneratorBuilder maxSize(int maxSize) {
        configDto.setMaxSize(maxSize);
        return this;
    }

    public MapGeneratorBuilder mapInstance(Supplier<Map<Object, Object>> mapSupplier) {
        configDto.setMapInstanceSupplier(mapSupplier);
        return this;
    }

    public MapGeneratorBuilder keyGenerator(IGenerator<Object> keyGenerator) {
        configDto.setKeyGenerator(keyGenerator);
        return this;
    }

    public MapGeneratorBuilder valueGenerator(IGenerator<Object> valueGenerator) {
        configDto.setKeyGenerator(valueGenerator);
        return this;
    }

    public MapGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    @Override
    public MapGenerator<Object, Object> build() {
        return build(configDto, false);
    }

    @Override
    public MapGenerator<Object, Object> build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        MapConfigDto mapConfig = (MapConfigDto) configDto;
        return new MapGenerator<>(
                mapConfig.getMinSize(),
                mapConfig.getMaxSize(),
                Objects.requireNonNull(mapConfig.getMapInstanceSupplier(), "Map instance supplier must be set"),
                Objects.requireNonNull(mapConfig.getKeyGenerator(), "Key generator must be set."),
                Objects.requireNonNull(mapConfig.getValueGenerator(), "Value generator must be set."),
                Objects.requireNonNull(mapConfig.getRuleRemark(), "Unexpected error, rule remark haven't set."));
    }
}
