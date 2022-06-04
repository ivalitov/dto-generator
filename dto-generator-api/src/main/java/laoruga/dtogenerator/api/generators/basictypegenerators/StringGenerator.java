package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;

import java.util.Arrays;

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
    private final String mask;
    private final char maskWildcard;
    private final char maskTypeMarker;


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
        if (mask != null && !mask.isEmpty()) {
            return generateStringForMask();
        } else {
            return generateString(length);
        }
    }

    private String generateString(int length) {
        return new RandomStringGenerator.Builder()
                .selectFrom(chars).build().generate(length);
    }

    /*
    1) parse mask      * 1) mask: +89 (%NUM%***) ***-**-** and charset: NUM
    1-1) get chars
    1-2) first iteration - replace asterics groups in oder types
     */

    private String generateStringForMask() {
        StringBuilder stringBuilder = new StringBuilder();
        char[] maskChars = mask.toCharArray();
        int begin = -1;
        int end = -1;
        int typeBegin = -1;
        int next;
        char[] partitionChars = chars;
        for (int current = 0; current < maskChars.length; current++) {
            if (maskChars[current] == maskWildcard) {
                next = current + 1;
                if (begin < 0) {
                    begin = current;
                } else if (maskChars.length == next) {
                    end = current;
                } else if (maskChars.length > next && maskChars[next] != maskWildcard) {
                    end = current;
                }
            } else if (maskChars[current] == maskTypeMarker) {
                if (typeBegin < 0) {
                    typeBegin = current;
                } else {
                    typeBegin = -1;
                    end = current;
                    String charSetName = new String(Arrays.copyOfRange(maskChars, typeBegin, maskChars[current] + 1));
                    try {
                        partitionChars = CharSet.valueOf(charSetName).getChars();
                    } catch (Exception e) {
                        log.debug("No charset found for name: '" + charSetName + "' for the mask: '" + mask + "'");
                    }
                }
            } else {
                stringBuilder.append(maskChars[current]);
            }
            if (end == current) {
                stringBuilder.append(
                        new RandomStringGenerator.Builder().selectFrom(partitionChars).build().generate(end - begin + 1));
                begin = -1;
                end = -1;
                typeBegin = -1;
                partitionChars = chars;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @return generator builder
     */
    public static StringGeneratorBuilder builder() {
        return new StringGeneratorBuilder();
    }

    public static final class StringGeneratorBuilder implements IGeneratorBuilder {
        private int maxLength = StringRules.DEFAULT_MAX_SYMBOLS_NUMBER;
        private int minLength = StringRules.DEFAULT_MIN_SYMBOLS_NUMBER;
        private CharSet[] charset = StringRules.DEFAULT_CHARSET;
        private String chars = StringRules.DEFAULT_CHARS;
        private IRuleRemark ruleRemark = StringRules.DEFAULT_RULE_REMARK;
        private String mask;
        private char maskWildcard;
        private char maskTypeMarker;

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

        public StringGeneratorBuilder mask(String mask) {
            this.mask = mask;
            return this;
        }

        public StringGeneratorBuilder maskWildcard(char maskWildcard) {
            this.maskWildcard = maskWildcard;
            return this;
        }

        public StringGeneratorBuilder maskTypeMarker(char maskTypeMarker) {
            this.maskTypeMarker = maskTypeMarker;
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
                    this.mask,
                    this.maskWildcard,
                    this.maskTypeMarker
            );
        }
    }
}
