package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.ArrayGenerator;
import org.laoruga.dtogenerator.generator.configs.ArrayConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;

import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public class ArrayGeneratorBuilder implements IGeneratorBuilderConfigurable<Object> {
    protected final ArrayConfigDto configDto;

    public ArrayGeneratorBuilder() {
        this.configDto = new ArrayConfigDto();
    }

    public ArrayGeneratorBuilder minSize(int minSize) {
        configDto.setMinSize(minSize);
        return this;
    }

    public ArrayGeneratorBuilder maxSize(int maxSize) {
        configDto.setMaxSize(maxSize);
        return this;
    }

    public ArrayGeneratorBuilder elementGenerator(IGenerator<?> elementGenerator) {
        configDto.setElementGenerator(elementGenerator);
        return this;
    }

    public ArrayGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public ArrayGenerator build() {
        return build(configDto, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        ArrayConfigDto collectionConfig = (ArrayConfigDto) configDto;
        return new ArrayGenerator(
                collectionConfig.getMinSize(),
                collectionConfig.getMaxSize(),
                Objects.requireNonNull(collectionConfig.getElementType(), "Array element type must be set"),
                Objects.requireNonNull(collectionConfig.getElementGenerator(), "Array element generator must be set"),
                Objects.requireNonNull(collectionConfig.getRuleRemark(), "Unexpected error, rule remark haven't set."));
    }

}