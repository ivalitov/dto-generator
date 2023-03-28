package org.laoruga.dtogenerator.generator;

import com.mifmif.common.regex.Generex;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.config.dto.StringConfigDto;
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

    public StringGenerator(StringConfigDto config) {
        maxLength = config.getMaxLength();
        minLength = config.getMinLength();
        chars = config.getChars().toCharArray();
        words = config.getWords();
        ruleRemark = Objects.requireNonNull(config.getRuleRemark(), "Rule remark not set.");
        regexp = config.getRegexp();
    }

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

}
