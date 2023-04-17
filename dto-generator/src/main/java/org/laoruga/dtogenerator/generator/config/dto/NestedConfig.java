package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Builder;
import lombok.Getter;
import org.laoruga.dtogenerator.DtoGeneratorBuildersTree;
import org.laoruga.dtogenerator.api.RuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 27.03.2023
 */
@Builder
@Getter
public class NestedConfig implements ConfigDto {

    private DtoGeneratorBuildersTree.Node dtoGeneratorBuilderTreeNode;
    private RuleRemark ruleRemark;

    @Override
    public void merge(ConfigDto from) {
        NestedConfig nestedConfigFrom = (NestedConfig) from;
        if (nestedConfigFrom.dtoGeneratorBuilderTreeNode != null) dtoGeneratorBuilderTreeNode = nestedConfigFrom.dtoGeneratorBuilderTreeNode;
        if (nestedConfigFrom.ruleRemark != null) ruleRemark = nestedConfigFrom.ruleRemark;
    }

    @Override
    public ConfigDto setRuleRemark(RuleRemark ruleRemark) {
        this.ruleRemark = ruleRemark;
        return this;
    }
}
