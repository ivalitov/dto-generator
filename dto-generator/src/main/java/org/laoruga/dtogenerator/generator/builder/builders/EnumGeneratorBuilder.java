package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.EnumGenerator;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.EnumConfigDto;

import java.util.Arrays;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class EnumGeneratorBuilder implements IGeneratorBuilderConfigurable {

    EnumConfigDto configDto;

    public EnumGeneratorBuilder() {
        this.configDto = new EnumConfigDto();
    }

    public EnumGeneratorBuilder possibleEnumNames(String... possibleEnumNames) {
        configDto.setPossibleEnumNames(possibleEnumNames);
        return this;
    }

    @SuppressWarnings("unchecked")
    public EnumGeneratorBuilder enumClass(Class<?> enumClass) {
        if (enumClass.isEnum()) {
            configDto.setEnumClass((Class<? extends Enum<?>>) enumClass);
        } else {
            throw new DtoGeneratorException(new ClassCastException("Enum class expected"));
        }
        return this;
    }

    public EnumGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public EnumGenerator build() {
        return build(configDto, false);
    }

    public EnumGenerator build(ConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        EnumConfigDto enumConfigDto = (EnumConfigDto) configDto;

        if (enumConfigDto.getEnumClass() == null) {
            throw new DtoGeneratorException("Enum class wasn't set for generator.");
        }
        if (enumConfigDto.getPossibleEnumNames().length == 0) {
            enumConfigDto.setPossibleEnumNames(Arrays
                    .stream(enumConfigDto.getEnumClass().getEnumConstants())
                    .map(Enum::name).toArray(String[]::new));
        }
        return new EnumGenerator(
                enumConfigDto.getPossibleEnumNames(),
                enumConfigDto.getEnumClass(),
                enumConfigDto.getRuleRemark());
    }

}
