package laoruga.dtogenerator.api.generators;

import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.RuleRemark;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.text.RandomStringGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BasicTypeGenerators {

    @AllArgsConstructor
    public static class DoubleGenerator implements IGenerator<Double> {

        private final double maxValue;
        private final double minValue;
        private final int precision;
        private final IRuleRemark ruleRemark;

        @Override
        public Double generate() {
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                return minValue;
            }
            if (ruleRemark == RuleRemark.MAX_VALUE) {
                return maxValue;
            }
            if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                double generated = minValue + new Random().nextDouble() * (maxValue - minValue);
                return Precision.round(generated, precision);
            }
            if (ruleRemark == RuleRemark.NULL_VALUE) {
                return null;
            }
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
    }

    @AllArgsConstructor
    public static class StringGenerator implements IGenerator<String> {

        private final int maxLength;
        private final int minLength;
        private final CharSet[] charset;
        private final String chars;
        private final IRuleRemark ruleRemark;

        @Override
        public String generate() {
            int length;
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                length = minLength;
            } else if (ruleRemark == RuleRemark.MAX_VALUE) {
                length = maxLength;
            } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                length = minLength + (int) (Math.random() * (maxLength - minLength));
            } else if (ruleRemark == RuleRemark.NULL_VALUE) {
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
    }

    @AllArgsConstructor
    public static class IntegerGenerator implements IGenerator<Integer> {

        private final int maxValue;
        private final int minValue;
        private final IRuleRemark ruleRemark;

        @Override
        public Integer generate() {
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                return minValue;
            }
            if (ruleRemark == RuleRemark.MAX_VALUE) {
                return maxValue;
            }
            if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                return new RandomDataGenerator().nextInt(minValue, maxValue);
            }
            if (ruleRemark == RuleRemark.NULL_VALUE) {
                return null;
            }
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
    }

    @AllArgsConstructor
    public static class LongGenerator implements IGenerator<Long> {

        private final long maxValue;
        private final long minValue;
        private final IRuleRemark ruleRemark;

        @Override
        public Long generate() {
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                return minValue;
            }
            if (ruleRemark == RuleRemark.MAX_VALUE) {
                return maxValue;
            }
            if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                return minValue + (long) (Math.random() * (maxValue - minValue));
            }
            if (ruleRemark == RuleRemark.NULL_VALUE) {
                return null;
            }
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
    }

    @AllArgsConstructor
    public static class EnumGenerator implements IGenerator<Enum<?>> {

        private final String[] possibleEnumNames;
        private final Class<? extends Enum<?>> enumClass;
        private final IRuleRemark ruleRemark;

        @Override
        @SneakyThrows
        public Enum<?> generate() {
            String[] sortedEnumNames = Arrays.stream(possibleEnumNames)
                    .sorted(Comparator.comparing(String::length))
                    .toArray(String[]::new);
            String enumInstanceName;
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                enumInstanceName = sortedEnumNames[0];
            } else if (ruleRemark == RuleRemark.MAX_VALUE) {
                enumInstanceName = sortedEnumNames[sortedEnumNames.length - 1];
            } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                int count = sortedEnumNames.length;
                enumInstanceName = sortedEnumNames[new Random().nextInt(count)];
            } else if (ruleRemark == RuleRemark.NULL_VALUE) {
                return null;
            } else {
                throw new IllegalStateException("Unexpected value " + ruleRemark);
            }
            for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                if (enumConstant.name().equals(enumInstanceName)) {
                    return enumConstant;
                }
            }
            throw new RuntimeException("Enum instance with name: '" + enumInstanceName +
                    "' has not been found in Class: '" + enumClass + "'");
        }
    }

    @AllArgsConstructor
    public static class LocalDateTimeGenerator implements IGenerator<LocalDateTime> {

        private final int leftShiftDays;
        private final int rightShiftDays;
        private final IRuleRemark ruleRemark;

        @Override
        public LocalDateTime generate() {
            LocalDateTime now = LocalDateTime.now();
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                return now.minusDays(leftShiftDays);
            }
            if (ruleRemark == RuleRemark.MAX_VALUE) {
                return now.plusDays(rightShiftDays);
            }
            if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                int randomInt = new Random().nextInt(leftShiftDays + rightShiftDays + 1);
                LocalDateTime minDate = now.minusDays(leftShiftDays);
                return minDate.plusDays(randomInt);
            }
            if (ruleRemark == RuleRemark.NULL_VALUE) {
                return null;
            }
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
    }

    public static class NullGenerator implements IGenerator<Object> {
        @Override
        public String generate() {
            return null;
        }
    }

    @AllArgsConstructor
    public static class ListGenerator implements IGenerator<List> {

        @Override
        public List generate() {
            return null;
        }
    }

}
