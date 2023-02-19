package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.CollectionGenerator;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.IConfigDto;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public class CollectionGeneratorBuilder<V> implements IGeneratorBuilderConfigurable {
    protected final CollectionConfigDto configDto;

    public CollectionGeneratorBuilder() {
        this.configDto = new CollectionConfigDto();
    }

    public CollectionGeneratorBuilder<V> minSize(int minSize) {
        configDto.setMinSize(minSize);
        return this;
    }

    public CollectionGeneratorBuilder<V> maxSize(int maxSize) {
        configDto.setMaxSize(maxSize);
        return this;
    }

    @SuppressWarnings("unchecked")
    public CollectionGeneratorBuilder<?> collectionInstance(Supplier<Collection<Object>> listInstance) {
        configDto.setCollectionInstance(listInstance);
        return this;
    }

    @SuppressWarnings("unchecked")
    public CollectionGeneratorBuilder<?> elementGenerator(IGenerator<?> elementGenerator) {
        configDto.setElementGenerator((IGenerator<Object>) elementGenerator);
        return this;
    }

    public CollectionGeneratorBuilder<?> ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public CollectionGenerator build() {
        return build(configDto, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionGenerator build(IConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        CollectionConfigDto collectionConfig = (CollectionConfigDto) configDto;
        return new CollectionGenerator(
                collectionConfig.getMinSize(),
                collectionConfig.getMaxSize(),
                Objects.requireNonNull(collectionConfig.getCollectionInstance(), "Collection instance must be set."),
                Objects.requireNonNull(collectionConfig.getElementGenerator(), "Collection element generator must be set"),
                Objects.requireNonNull(collectionConfig.getRuleRemark(), "Unexpected error, rule remark haven't set."));
    }

}
