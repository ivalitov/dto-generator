package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.util.ReflectionUtils;
import org.laoruga.dtogenerator.util.dummy.DummyCollectionClass;

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
public class CollectionConfig implements ConfigDto {
    private Integer minSize;
    private Integer maxSize;
    private Supplier<?> collectionInstanceSupplier;
    private Generator<?> elementGenerator;
    private RuleRemark ruleRemark;

    public CollectionConfig(CollectionRule rule) {
        this.minSize = rule.minSize();
        this.maxSize = rule.maxSize();
        this.collectionInstanceSupplier = rule.collectionClass() != DummyCollectionClass.class
                ? () -> ReflectionUtils.createInstance(rule.collectionClass())
                : null;
        this.ruleRemark = rule.boundary();
    }

    public void merge(CollectionConfig from) {
        if (from.getMinSize() != null) this.minSize = from.getMinSize();
        if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
        if (from.getCollectionInstanceSupplier() != null) this.collectionInstanceSupplier = from.getCollectionInstanceSupplier();
        if (from.getElementGenerator() != null) this.elementGenerator = from.getElementGenerator();
        if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
    }

    @Override
    public void merge(ConfigDto configDto) {
        CollectionConfig fromConfig = (CollectionConfig) configDto;
        if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
        if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
        if (fromConfig.getCollectionInstanceSupplier() != null)
            this.collectionInstanceSupplier = fromConfig.getCollectionInstanceSupplier();
        if (fromConfig.getElementGenerator() != null) this.elementGenerator = fromConfig.getElementGenerator();
        if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
    }
}
