package org.laoruga.dtogenerator.generators;

import com.mifmif.common.regex.Generex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
@Slf4j
public class StringGenerator implements IGenerator<String> {

    private final int maxLength;
    private final int minLength;
    private final char[] chars;
    private final String[] words;
    private final IRuleRemark ruleRemark;
    private final String regexp;

    @Override
    public String generate() {
        int length;
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            length = minLength;
        } else if (ruleRemark == RuleRemark.MAX_VALUE) {
            length = maxLength;
        } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            length = RandomUtils.nextInt(minLength, maxLength);
        } else if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
        if (words.length != 0) {
            return getRandomWord();
        }
        if (regexp != null && !regexp.isEmpty()) {
            return generateStringByRegexp();
        }
        return generateString(length);

    }

    private String generateString(int length) {
        return RandomUtils.nextString(chars, length);
    }

    private String generateStringByRegexp() {
        return new Generex(regexp).random(minLength, maxLength);
    }

    private String getRandomWord() {
        String randomItemFromList = RandomUtils.getRandomItem(words);
        if (maxLength < randomItemFromList.length()) {
            randomItemFromList = randomItemFromList.substring(0, maxLength);
        }
        return randomItemFromList;
    }

    /**
     * @return generator builder
     */
    public static StringGeneratorBuilder builder() {
        return new StringGeneratorBuilder();
    }

    public static final class StringGeneratorBuilder implements IGeneratorBuilderConfigurable {

        private final ConfigDto configDto;

        private StringGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public StringGeneratorBuilder maxLength(int maxLength) {
            this.configDto.maxLength = maxLength;
            return this;
        }

        public StringGeneratorBuilder minLength(int minLength) {
            this.configDto.minLength = minLength;
            return this;
        }

        public StringGeneratorBuilder words(String... words) {
            this.configDto.words = words;
            return this;
        }

        public StringGeneratorBuilder chars(String chars) {
            this.configDto.chars = chars;
            return this;
        }

        public StringGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.configDto.ruleRemark = ruleRemark;
            return this;
        }

        public StringGeneratorBuilder regexp(String regexp) {
            this.configDto.regexp = regexp;
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
            ConfigDto stringConfigDto = (ConfigDto) configDto;
            return new StringGenerator(
                    stringConfigDto.maxLength,
                    stringConfigDto.minLength,
                    stringConfigDto.chars.toCharArray(),
                    stringConfigDto.words,
                    Objects.requireNonNull(stringConfigDto.ruleRemark, "Rule remark not set."),
                    stringConfigDto.regexp
            );
        }

    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto {

        private Integer maxLength;
        private Integer minLength;
        private String[] words;
        private String chars;
        private IRuleRemark ruleRemark;
        private String regexp;

        public ConfigDto(StringRule stringRule) {
            this.maxLength = stringRule.maxLength();
            this.minLength = stringRule.minLength();
            this.words = stringRule.words();
            this.chars = stringRule.chars();
            this.ruleRemark = stringRule.ruleRemark();
            this.regexp = stringRule.regexp();
        }

        public ConfigDto() {}

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return StringGeneratorBuilder.class;
        }

        public void merge(IConfigDto from) {
            ConfigDto configDto = (ConfigDto) from;
            if (configDto.getMaxLength() != null) this.maxLength = configDto.getMaxLength();
            if (configDto.getMinLength() != null) this.minLength = configDto.getMinLength();
            if (configDto.getWords() != null) this.words = configDto.getWords();
            if (configDto.getChars() != null) this.chars = configDto.getChars();
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
            if (configDto.getRegexp() != null) this.regexp = configDto.getRegexp();
        }
    }

}
