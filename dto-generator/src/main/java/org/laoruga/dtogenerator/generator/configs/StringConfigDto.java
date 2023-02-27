package org.laoruga.dtogenerator.generator.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.generator.builder.builders.StringGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StringConfigDto implements ConfigDto {

    private Integer maxLength;
    private Integer minLength;
    private String[] words;
    private String chars;
    private IRuleRemark ruleRemark;
    private String regexp;

    public StringConfigDto(StringRule stringRule) {
        this.maxLength = stringRule.maxLength();
        this.minLength = stringRule.minLength();
        this.words = stringRule.words();
        this.chars = stringRule.chars();
        this.ruleRemark = stringRule.ruleRemark();
        this.regexp = stringRule.regexp();
    }

    @Override
    public Class<? extends IGeneratorBuilder> getBuilderClass() {
        return StringGeneratorBuilder.class;
    }

    public void merge(ConfigDto from) {
        StringConfigDto configDto = (StringConfigDto) from;
        if (configDto.getMaxLength() != null) this.maxLength = configDto.getMaxLength();
        if (configDto.getMinLength() != null) this.minLength = configDto.getMinLength();
        if (configDto.getWords() != null) this.words = configDto.getWords();
        if (configDto.getChars() != null) this.chars = configDto.getChars();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        if (configDto.getRegexp() != null) this.regexp = configDto.getRegexp();
    }
}
