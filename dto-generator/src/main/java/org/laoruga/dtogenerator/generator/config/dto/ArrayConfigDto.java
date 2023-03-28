package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
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
public class ArrayConfigDto implements ConfigDto {
    private Integer minSize;
    private Integer maxSize;
    private Class<?> elementType;
    private IGenerator<?> elementGenerator;
    private IRuleRemark ruleRemark;

    public ArrayConfigDto(ArrayRule rule, Class<?> elementType) {
        this.minSize = rule.minSize();
        this.maxSize = rule.maxSize();
        this.elementType = elementType;
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(ArrayConfigDto from) {
        if (from.getMinSize() != null) this.minSize = from.getMinSize();
        if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
        if (from.getElementType() != null) this.elementType = from.getElementType();
        if (from.getElementGenerator() != null) this.elementGenerator = from.getElementGenerator();
        if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
    }

    @Override
    public void merge(ConfigDto configDto) {
        ArrayConfigDto fromConfig = (ArrayConfigDto) configDto;
        if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
        if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
        if (fromConfig.getElementType() != null)
            this.elementType = fromConfig.getElementType();
        if (fromConfig.getElementGenerator() != null) this.elementGenerator = fromConfig.getElementGenerator();
        if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
    }
}
