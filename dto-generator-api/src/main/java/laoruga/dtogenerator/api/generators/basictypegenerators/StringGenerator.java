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
    private final Character maskWildcard;
    private final Character maskTypeMarker;


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
     */


    private String generateStringForMask() {
        StringBuilder stringBuilder = new StringBuilder();
        char[] maskChars = mask.toCharArray();
        int wildCardBeginIdx = 0;
        int wildCardEndIdx = 0;
        boolean wildcardGathering = false;
        boolean wildcardGathered = false;
        char[] partitionChars = chars;
        char currentChar;
        Character nextChar;
        boolean typeGathering = false;
        StringBuilder typeChars = null;
        for (int currentPos = 0; currentPos < maskChars.length; currentPos++) {
            currentChar = maskChars[currentPos];
            nextChar = maskChars.length == currentPos + 1 ? null : maskChars[currentPos + 1];
            if (currentChar == maskWildcard) {
                if (wildcardGathering) {
                    wildCardEndIdx = currentPos;
                } else {
                    wildcardGathering = true;
                    wildCardBeginIdx = currentPos;
                    wildCardEndIdx = currentPos;
                }
                if (nextChar == maskWildcard) {
                    continue;
                } else {
                    wildcardGathering = false;
                    wildcardGathered = true;
                }
            } else if (currentChar == maskTypeMarker) {
                // begin of type gathering
                if (!typeGathering) {
                    // exclusion of paris: %% %*
                    if (nextChar != null && nextChar != maskWildcard && nextChar != maskTypeMarker) {
                        typeGathering = true;
                        typeChars = new StringBuilder();
                        continue;
                    }
                }
                if (typeGathering) {
                    CharSet charSet = CharSet.getCharSetOrNull(typeChars.toString());
                    if (nextChar == maskWildcard && charSet != null) {
                        partitionChars = charSet.getChars();
                        typeGathering = false;
                        continue;
                    } else {
                        stringBuilder.append(maskChars);
                        currentPos--;
                        continue;
                    }
                }
            }
            if (typeGathering) {
                typeChars.append(currentChar);
            } else if (wildcardGathered) {
                //appending generated substring instead of wildcard substring
                stringBuilder.append(
                        new RandomStringGenerator.Builder().selectFrom(partitionChars).build().generate(
                                wildCardEndIdx - wildCardBeginIdx + 1));
                partitionChars = chars;
                wildcardGathered = false;
            } else {
                //appending char as is
                stringBuilder.append(currentChar);
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
        private String mask = StringRules.DEFAULT_MASK;
        private char maskWildcard = StringRules.DEFAULT_WILDCARD;
        private char maskTypeMarker = StringRules.DEFAULT_TYPE_MARKER;

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
