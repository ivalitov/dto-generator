package dtogenerator.api.factory;

import dtogenerator.api.constants.CharSet;
import dtogenerator.api.markup.IGenerator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.text.RandomStringGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

public class BasicTypeGenerators {

    @AllArgsConstructor
    public static class DoubleGenerator implements IGenerator<Double> {

        private final double maxValue;
        private final double minValue;
        private final int precision;

        @Override
        public Double generate() {
            double generated = minValue + new Random().nextDouble() * (maxValue - minValue);
            return Precision.round(generated, precision);
        }
    }

    @AllArgsConstructor
    static class StringFieldGenerator implements IGenerator<String> {

        private final int maxLength;
        private final int minLength;
        private final CharSet[] charset;

        @Override
        public String generate() {
            int length = minLength + (int) (Math.random() * (maxLength - minLength));
            Integer charsCount = Arrays.stream(charset).map(s -> s.getChars().length).reduce(Integer::sum).get();
            char[] chars = new char[charsCount];
            int nextCopyPos = 0;
            for (CharSet charSet : charset) {
                char[] toCopy = charSet.getChars();
                System.arraycopy(toCopy, 0, chars, nextCopyPos, toCopy.length);
                nextCopyPos += toCopy.length;
            }
            return new RandomStringGenerator.Builder()
                    .selectFrom(chars)
                    .build().generate(length);
        }
    }

    @AllArgsConstructor
    static class IntegerFieldGenerator implements IGenerator<Long> {

        private final long maxValue;
        private final long minValue;

        @Override
        public Long generate() {
            return minValue + (long) (Math.random() * (maxValue - minValue));
        }
    }

    @AllArgsConstructor
    static class EnumFieldGenerator implements IGenerator<Enum<?>> {

        private final String[] possibleEnumNames;
        private final Class<? extends Enum<?>> enumClass;

        @Override
        @SneakyThrows
        public Enum<?> generate() {
            int count = possibleEnumNames.length;
            String enumInstanceName = possibleEnumNames[new Random().nextInt(count)];
            for (Object enumConstant : enumClass.getEnumConstants()) {
                if (((Enum<?>) enumConstant).name().equals(enumInstanceName)) {
                    return (Enum<?>) enumConstant;
                }
            }
            throw new RuntimeException("Enum instance with name: '" + enumInstanceName +
                    "' has not been found in Class: '" + enumClass + "'");
        }
    }

    @AllArgsConstructor
    static class LocalDateTimeFieldGenerator implements IGenerator<LocalDateTime> {

        private final int leftShiftDays;
        private final int rightShiftDays;

        @Override
        public LocalDateTime generate() {
            LocalDateTime minDate = LocalDateTime.now().minusDays(leftShiftDays);
            int randomInt = new Random().nextInt(leftShiftDays + rightShiftDays + 1);
            return minDate.plusDays(randomInt);
        }
    }

    static class NullGenerator implements IGenerator<Object> {
        @Override
        public String generate() {
            return null;
        }
    }
}
