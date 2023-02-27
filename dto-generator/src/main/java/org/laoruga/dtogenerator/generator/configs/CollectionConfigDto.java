package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.api.rules.SetRule;
import org.laoruga.dtogenerator.generator.builder.builders.CollectionGeneratorBuilder;
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
public class CollectionConfigDto implements ConfigDto {
    private Integer minSize;
    private Integer maxSize;
    private Supplier<Collection<Object>> collectionInstance;
    private IGenerator<Object> elementGenerator;
    private IRuleRemark ruleRemark;

    @SuppressWarnings("unchecked")
    public CollectionConfigDto(SetRule rule) {
        this.minSize = rule.minSize();
        this.maxSize = rule.maxSize();
        this.collectionInstance = () -> ReflectionUtils.createCollectionInstance(rule.setClass());
        this.ruleRemark = rule.ruleRemark();
    }

    @SuppressWarnings("unchecked")
    public CollectionConfigDto(ListRule rule) {
        this.minSize = rule.minSize();
        this.maxSize = rule.maxSize();
        this.collectionInstance = () -> ReflectionUtils.createCollectionInstance(rule.listClass());
        this.ruleRemark = rule.ruleRemark();
    }

    public void merge(CollectionConfigDto from) {
        if (from.getMinSize() != null) this.minSize = from.getMinSize();
        if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
        if (from.getCollectionInstance() != null) this.collectionInstance = from.getCollectionInstance();
        if (from.getElementGenerator() != null) this.elementGenerator = from.getElementGenerator();
        if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return CollectionGeneratorBuilder.class;
    }

    @Override
    public void merge(ConfigDto from) {
        CollectionConfigDto fromConfig = (CollectionConfigDto) from;
        if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
        if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
        if (fromConfig.getCollectionInstance() != null)
            this.collectionInstance = fromConfig.getCollectionInstance();
        if (fromConfig.getElementGenerator() != null) this.elementGenerator = fromConfig.getElementGenerator();
        if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
    }
}
