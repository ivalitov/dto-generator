package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.AllArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;

import java.util.Arrays;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class StringGenerator implements IGenerator<String> {

    private final int maxLength;
    private final int minLength;
    private final CharSet[] charset;
    private final String chars;
    private final IRuleRemark ruleRemark;

    @Override
    public String generate() {
        int length;
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            length = minLength;
        } else if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            length = maxLength;
        } else if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            length = minLength + (int) (Math.random() * (maxLength - minLength));
        } else if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
        char[] explicitChars = this.chars.toCharArray();
        int charsCount = explicitChars.length + Arrays.stream(charset).map(s -> s.getChars().length).reduce(Integer::sum).get();
        char[] chars = new char[charsCount];
        int nextCopyPos = 0;
        for (CharSet charSet : charset) {
            char[] toCopy = charSet.getChars();
            System.arraycopy(toCopy, 0, chars, nextCopyPos, toCopy.length);
            nextCopyPos += toCopy.length;
        }
        if (explicitChars.length != 0) {
            System.arraycopy(explicitChars, 0, chars, nextCopyPos, explicitChars.length);
        }
        return new RandomStringGenerator.Builder()
                .selectFrom(chars)
                .build().generate(length);
    }

    public static StringGeneratorBuilder builder() {
        return new StringGeneratorBuilder();
    }

    public static final class StringGeneratorBuilder implements IGeneratorBuilder {
        private int maxLength = StringRules.DEFAULT_MAX_SYMBOLS_NUMBER;
        private int minLength = StringRules.DEFAULT_MIN_SYMBOLS_NUMBER;
        private CharSet[] charset = StringRules.DEFAULT_CHARSET;
        private String chars = StringRules.DEFAULT_CHARS;
        private IRuleRemark ruleRemark = StringRules.DEFAULT_RULE_REMARK;

        private StringGeneratorBuilder() {}

        public StringGeneratorBuilder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public StringGeneratorBuilder minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public StringGeneratorBuilder charset(CharSet[] charset) {
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

        @Override
        public StringGenerator build() {
            return new StringGenerator(
                    this.maxLength,
                    this.minLength,
                    this.charset,
                    this.chars,
                    this.ruleRemark
            );
        }

    }
}
