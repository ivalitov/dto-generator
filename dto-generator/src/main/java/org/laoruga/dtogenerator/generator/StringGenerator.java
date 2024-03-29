package org.laoruga.dtogenerator.generator;

import com.mifmif.common.regex.Generex;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
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
        if (words.length > 0) {
            for (String word : words) {
                if (word.length() < minLength) {
                    throw new DtoGeneratorException("Length of the word ''" + word + "' " +
                            "less then required: '" + minLength + "'");
                } else if (word.length() > maxLength) {
                    throw new DtoGeneratorException("Length of the word ''" + word + "' " +
                            "greater then required: '" + maxLength + "'");
                }
            }
        }
    }

    @Override
    public String generate() {
        int length;

        switch ((Boundary) ruleRemark) {

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
        String generatedString = new Generex(regexp).random(minLength, maxLength);
        int generatedStringLength = generatedString.length();
        if (generatedStringLength < minLength) {
            throw new DtoGeneratorException(
                    "Length of string generated by regexp is less then required: '" + minLength + "'" +
                            " Generated string: " + generatedString);
        } else if (generatedStringLength > maxLength) {
            throw new DtoGeneratorException(
                    "Length of string generated by regexp is greater then required: '" + maxLength + "'" +
                            " Generated string: " + generatedString);
        }
        return generatedString;
    }

    private String getRandomWord() {
        return RandomUtils.getRandomItem(words);
    }

}
