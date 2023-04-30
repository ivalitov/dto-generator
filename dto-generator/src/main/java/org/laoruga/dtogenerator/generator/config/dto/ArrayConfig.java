package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.rules.ArrayRule;

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
public class ArrayConfig implements ConfigDto {
    private Integer minSize;
    private Integer maxSize;
    private Class<?> elementType;
    private Generator<?> elementGenerator;
    private RuleRemark ruleRemark;

    public ArrayConfig(ArrayRule rule, Class<?> elementType) {
        this.minSize = rule.minLength();
        this.maxSize = rule.maxLength();
        this.elementType = elementType;
        this.ruleRemark = rule.boundary();
    }

    public void merge(ArrayConfig from) {
        if (from.getMinSize() != null) this.minSize = from.getMinSize();
        if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
        if (from.getElementType() != null) this.elementType = from.getElementType();
        if (from.getElementGenerator() != null) this.elementGenerator = from.getElementGenerator();
        if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
    }

    @Override
    public void merge(ConfigDto configDto) {
        ArrayConfig fromConfig = (ArrayConfig) configDto;
        if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
        if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
        if (fromConfig.getElementType() != null)
            this.elementType = fromConfig.getElementType();
        if (fromConfig.getElementGenerator() != null) this.elementGenerator = fromConfig.getElementGenerator();
        if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
    }
}
