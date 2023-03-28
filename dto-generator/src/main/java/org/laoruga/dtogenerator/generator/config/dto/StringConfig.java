package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.StringRule;

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
public class StringConfig implements ConfigDto {

    private Integer maxLength;
    private Integer minLength;
    private String[] words;
    private String chars;
    private IRuleRemark ruleRemark;
    private String regexp;

    public StringConfig(StringRule stringRule) {
        this.maxLength = stringRule.maxLength();
        this.minLength = stringRule.minLength();
        this.words = stringRule.words();
        this.chars = stringRule.chars();
        this.ruleRemark = stringRule.ruleRemark();
        this.regexp = stringRule.regexp();
    }

    public void merge(ConfigDto from) {
        StringConfig configDto = (StringConfig) from;
        if (configDto.getMaxLength() != null) this.maxLength = configDto.getMaxLength();
        if (configDto.getMinLength() != null) this.minLength = configDto.getMinLength();
        if (configDto.getWords() != null) this.words = configDto.getWords();
        if (configDto.getChars() != null) this.chars = configDto.getChars();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        if (configDto.getRegexp() != null) this.regexp = configDto.getRegexp();
    }
}
