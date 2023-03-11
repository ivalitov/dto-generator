package org.laoruga.dtogenerator.generator.configs;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.util.DummyCollectionClass;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.util.Collection;
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
public class CollectionConfigDto implements ConfigDto {
    private Integer minSize;
    private Integer maxSize;
    private Supplier<Collection<?>> collectionInstanceSupplier;
    private IGenerator<?> elementGenerator;
    private IRuleRemark ruleRemark;

    public CollectionConfigDto(CollectionRule rule) {
        this.minSize = rule.minSize();
        this.maxSize = rule.maxSize();
        this.collectionInstanceSupplier = rule.collectionClass() != DummyCollectionClass.class
                ? () -> ReflectionUtils.createCollectionInstance(rule.collectionClass())
                : null;
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(CollectionConfigDto from) {
        if (from.getMinSize() != null) this.minSize = from.getMinSize();
        if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
        if (from.getCollectionInstanceSupplier() != null) this.collectionInstanceSupplier = from.getCollectionInstanceSupplier();
        if (from.getElementGenerator() != null) this.elementGenerator = from.getElementGenerator();
        if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
    }

    @Override
    public void merge(ConfigDto configDto) {
        CollectionConfigDto fromConfig = (CollectionConfigDto) configDto;
        if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
        if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
        if (fromConfig.getCollectionInstanceSupplier() != null)
            this.collectionInstanceSupplier = fromConfig.getCollectionInstanceSupplier();
        if (fromConfig.getElementGenerator() != null) this.elementGenerator = fromConfig.getElementGenerator();
        if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
    }
}
