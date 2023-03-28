package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyMapClass;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapConfig implements ConfigDto {
    private Integer minSize;
    private Integer maxSize;
    private Supplier<Map<Object, Object>> mapInstanceSupplier;
    private IGenerator<Object> keyGenerator;
    private IGenerator<Object> valueGenerator;
    private IRuleRemark ruleRemark;

    public MapConfig(MapRule rule) {
        this.minSize = rule.minSize();
        this.maxSize = rule.maxSize();
        this.mapInstanceSupplier = rule.mapClass() != DummyMapClass.class
                ? () -> ReflectionUtils.createInstance(rule.mapClass())
                : null;
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(MapConfig from) {
        if (from.getMinSize() != null) this.minSize = from.getMinSize();
        if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
        if (from.getMapInstanceSupplier() != null) this.mapInstanceSupplier = from.getMapInstanceSupplier();
        if (from.getKeyGenerator() != null) this.keyGenerator = from.getKeyGenerator();
        if (from.getValueGenerator() != null) this.valueGenerator = from.getValueGenerator();
        if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
    }

    @Override
    public void merge(ConfigDto configDto) {
        MapConfig fromConfig = (MapConfig) configDto;
        if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
        if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
        if (fromConfig.getMapInstanceSupplier() != null)
            this.mapInstanceSupplier = fromConfig.getMapInstanceSupplier();
        if (fromConfig.getKeyGenerator() != null) this.keyGenerator = fromConfig.getKeyGenerator();
        if (fromConfig.getValueGenerator() != null) this.valueGenerator = fromConfig.getValueGenerator();
        if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
    }
}
