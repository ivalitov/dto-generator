package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.CollectionGenerator;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public class CollectionGeneratorBuilder implements IGeneratorBuilderConfigurable<Collection<?>> {
    protected final CollectionConfigDto configDto;

    public CollectionGeneratorBuilder() {
        this.configDto = new CollectionConfigDto();
    }

    public CollectionGeneratorBuilder minSize(int minSize) {
        configDto.setMinSize(minSize);
        return this;
    }

    public CollectionGeneratorBuilder maxSize(int maxSize) {
        configDto.setMaxSize(maxSize);
        return this;
    }

    public CollectionGeneratorBuilder collectionInstance(Supplier<Collection<?>> listInstance) {
        configDto.setCollectionInstanceSupplier(listInstance);
        return this;
    }

    public CollectionGeneratorBuilder elementGenerator(IGenerator<?> elementGenerator) {
        configDto.setElementGenerator(elementGenerator);
        return this;
    }

    public CollectionGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public CollectionGenerator build() {
        return build(configDto, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CollectionGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        CollectionConfigDto collectionConfig = (CollectionConfigDto) configDto;
        return new CollectionGenerator(
                collectionConfig.getMinSize(),
                collectionConfig.getMaxSize(),
                Objects.requireNonNull(collectionConfig.getCollectionInstanceSupplier(), "Collection instance must be set."),
                Objects.requireNonNull(collectionConfig.getElementGenerator(), "Collection element generator must be set"),
                Objects.requireNonNull(collectionConfig.getRuleRemark(), "Unexpected error, rule remark haven't set."));
    }

}
