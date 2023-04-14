package org.laoruga.dtogenerator.generator;

import com.mifmif.common.regex.Generex;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.generator.config.dto.StringConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
@Slf4j
public class StringGenerator implements Generator<String> {

    private final int maxLength;
    private final int minLength;
    private final char[] chars;
    private final String[] words;
    private final RuleRemark ruleRemark;
    private final String regexp;

    public StringGenerator(StringConfig config) {
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

        switch ((BoundaryConfig) ruleRemark) {

            case MIN_VALUE:
                length = minLength;
                break;

            case MAX_VALUE:
                length = maxLength;
                break;

            case NULL_VALUE:
                return null;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                length = RandomUtils.nextInt(minLength, maxLength);
                break;

            default:
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
