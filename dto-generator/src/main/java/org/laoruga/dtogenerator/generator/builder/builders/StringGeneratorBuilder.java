package org.laoruga.dtogenerator.generator.builder.builders;

import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.StringGenerator;
import org.laoruga.dtogenerator.generator.configs.IConfigDto;
import org.laoruga.dtogenerator.generator.configs.StringConfigDto;

import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
public final class StringGeneratorBuilder implements IGeneratorBuilderConfigurable {

    private final StringConfigDto configDto;

    public StringGeneratorBuilder() {
        this.configDto = new StringConfigDto();
    }

    public StringGeneratorBuilder maxLength(int maxLength) {
        this.configDto.setMaxLength(maxLength);
        return this;
    }

    public StringGeneratorBuilder minLength(int minLength) {
        this.configDto.setMinLength(minLength);
        return this;
    }

    public StringGeneratorBuilder words(String... words) {
        this.configDto.setWords(words);
        return this;
    }

    public StringGeneratorBuilder chars(String chars) {
        this.configDto.setChars(chars);
        return this;
    }

    public StringGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
        this.configDto.setRuleRemark(ruleRemark);
        return this;
    }

    public StringGeneratorBuilder regexp(String regexp) {
        this.configDto.setRegexp(regexp);
        return this;
    }

    @Override
    public StringGenerator build() {
        return build(this.configDto, false);
    }

    public StringGenerator build(IConfigDto configDto, boolean merge) {
        if (merge) {
            configDto.merge(this.configDto);
        }
        StringConfigDto stringConfigDto = (StringConfigDto) configDto;
        return new StringGenerator(
                stringConfigDto.getMaxLength(),
                stringConfigDto.getMinLength(),
                stringConfigDto.getChars().toCharArray(),
                stringConfigDto.getWords(),
                Objects.requireNonNull(stringConfigDto.getRuleRemark(), "Rule remark not set."),
                stringConfigDto.getRegexp()
        );
    }

}
