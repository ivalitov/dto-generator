package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.EnumRule;
import org.laoruga.dtogenerator.generator.builder.builders.EnumGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
public class EnumConfigDto implements IConfigDto {
    private String[] possibleEnumNames;
    private Class<? extends Enum<?>> enumClass;
    private IRuleRemark ruleRemark;

    public EnumConfigDto(EnumRule enumRule) {
        possibleEnumNames = enumRule.possibleEnumNames();
        ruleRemark = enumRule.ruleRemark();
    }

    public EnumConfigDto() {
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return EnumGeneratorBuilder.class;
    }

    public void merge(IConfigDto from) {
        EnumConfigDto configDto = (EnumConfigDto) from;
        if (configDto.getPossibleEnumNames() != null) this.possibleEnumNames = configDto.getPossibleEnumNames();
        if (configDto.getEnumClass() != null) this.enumClass = configDto.getEnumClass();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }
}
