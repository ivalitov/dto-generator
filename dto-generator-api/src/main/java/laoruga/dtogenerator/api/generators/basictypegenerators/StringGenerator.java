package laoruga.dtogenerator.api.generators.basictypegenerators;

import com.mifmif.common.regex.Generex;
import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import laoruga.dtogenerator.api.util.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;

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
    private final IRuleRemark ruleRemark;
    private final String regexp;

    @Override
    public String generate() {
        int length;
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            length = minLength;
        } else if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            length = maxLength;
        } else if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            length = RandomUtils.nextInt(minLength, maxLength);
        } else if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
        if (regexp != null && !regexp.isEmpty()) {
            return generateStringByRegexp();
        } else {
            return generateString(length);
        }
    }

    private String generateString(int length) {
        return new RandomStringGenerator.Builder()
                .selectFrom(chars).build().generate(length);
    }

    private String generateStringByRegexp() {
        return new Generex(regexp).random(minLength, maxLength);
    }

    /**
     * @return generator builder
     */
    public static StringGeneratorBuilder builder() {
        return new StringGeneratorBuilder();
    }

    public static final class StringGeneratorBuilder implements IGeneratorBuilder<IGenerator<?>> {
        private int maxLength = StringRule.DEFAULT_MAX_SYMBOLS_NUMBER;
        private int minLength = StringRule.DEFAULT_MIN_SYMBOLS_NUMBER;
        private CharSet[] charset = StringRule.DEFAULT_CHARSET;
        private String chars = StringRule.DEFAULT_CHARS;
        private IRuleRemark ruleRemark = StringRule.DEFAULT_RULE_REMARK;
        private String regexp = StringRule.DEFAULT_REGEXP;

        private StringGeneratorBuilder() {
        }

        public StringGeneratorBuilder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public StringGeneratorBuilder minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public StringGeneratorBuilder charset(CharSet... charset) {
            this.charset = charset;
            return this;
        }

        public StringGeneratorBuilder chars(String chars) {
            this.chars = chars;
            return this;
        }

        public StringGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public StringGeneratorBuilder regexp(String regexp) {
            this.regexp = regexp;
            return this;
        }

        private char[] getChars() {
            if (charset.length == 1) {
                return charset[0].getChars();
            }
            int newLength = this.chars.length();
            int nextCopyPos = 0;
            for (CharSet charSet : charset) {
                newLength += charSet.getChars().length;
            }
            char[] resultChars = new char[newLength];
            for (CharSet charSet : charset) {
                System.arraycopy(charSet.getChars(), 0, resultChars, nextCopyPos, charSet.getChars().length);
                nextCopyPos += charSet.getChars().length;
            }
            System.arraycopy(chars.toCharArray(), 0, resultChars, nextCopyPos, chars.length());
            return resultChars;
        }

        @Override
        public StringGenerator build() {
            return new StringGenerator(
                    this.maxLength,
                    this.minLength,
                    getChars(),
                    this.ruleRemark,
                    this.regexp
            );
        }
    }
}
