package laoruga.factory;

import laoruga.CharSet;
import laoruga.custom.ChBusinessRule;
import laoruga.dto.Arrears;
import laoruga.dto.DtoVer1;
import laoruga.markup.ICustomGenerator;
import laoruga.markup.CustomRules;
import laoruga.markup.IGenerator;
import laoruga.markup.bounds.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.text.RandomStringGenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DtoBuilder {


    public static void main(String[] args) throws IllegalAccessException {
        GenerationFactory.getInstance().registerCustomGenerator(ChBusinessRule.class, ArrearsGenerator.class);
        generateDto(DtoVer1.class);
    }

    @SneakyThrows
    static void generateDto(Class dtoClass) {
        Object dtoInstance = dtoClass.newInstance();
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            IGenerator<?> generator = selectGenerator(field);
            field.setAccessible(true);
            field.set(dtoInstance, generator.generate());
            field.setAccessible(false);
        }
        System.out.println(dtoInstance);
    }

    static IGenerator<?> selectGenerator(Field field) {

        if (field.getType() == Double.class) {
            DecimalFieldBounds decimalBounds = field.getAnnotation(DecimalFieldBounds.class);
            if (decimalBounds != null) {
                return new DecimalFieldGenerator(
                        decimalBounds.maxValue(),
                        decimalBounds.minValue(),
                        decimalBounds.precision()
                );
            }
        }

        if (field.getType() == String.class) {
            StringFieldBounds stringBounds = field.getAnnotation(StringFieldBounds.class);
            if (stringBounds != null) {
                return new StringFieldGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols(),
                        stringBounds.charset()
                );
            }
        }

        if (field.getType() == Long.class) {
            LongFieldBounds stringBounds = field.getAnnotation(LongFieldBounds.class);
            if (stringBounds != null) {
                return new IntegerFieldGenerator(
                        stringBounds.maxValue(),
                        stringBounds.minValue()
                );
            }
        }

        if (field.getType().isEnum()) {
            EnumFieldBounds enumBounds = field.getAnnotation(EnumFieldBounds.class);
            if (enumBounds != null) {
                return new EnumFieldGenerator(
                        enumBounds.possibleValues(),
                        enumBounds.className()
                );
            }
        }

        if (field.getType() == LocalDateTime.class) {
            LocalDateTimeFieldBounds enumBounds = field.getAnnotation(LocalDateTimeFieldBounds.class);
            if (enumBounds != null) {
                return new LocalDateTimeFieldGenerator(
                        enumBounds.leftShiftDays(),
                        enumBounds.rightShiftDays()
                );
            }
        }

        List<Annotation> customGenerators = Arrays.stream(field.getAnnotations())
                .filter(a -> a.annotationType().getAnnotation(CustomRules.class) != null)
                .collect(Collectors.toList());

        if (!customGenerators.isEmpty()) {
            GenerationFactory genFactory = GenerationFactory.getInstance();
            for (Annotation generatorMarker : customGenerators) {
                if (genFactory.isCustomGeneratorExists(generatorMarker.annotationType())) {
                    return genFactory.getCustomGenerator(generatorMarker.annotationType());
//                    ICustomGenerator customGenerator = genFactory.getCustomGenerator(generatorMarker.annotationType());
//                    Object generate = customGenerator.generate(generatorMarker);
                } else {
                    throw new RuntimeException();
                }
            }
        }

        return new NullGenerator();
    }

    @AllArgsConstructor
    static class DecimalFieldGenerator implements IGenerator<Double> {

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

        private final String[] possibleValues;
        private final String className;

        @Override
        @SneakyThrows
        public Enum<?> generate() {
            int count = possibleValues.length;
            String enumInstanceName = possibleValues[new Random().nextInt(count)];
            Class<?> aClass;
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw e;
            }
            if (aClass.isEnum()) {
                for (Object enumConstant : aClass.getEnumConstants()) {
                    if (((Enum<?>) enumConstant).name().equals(enumInstanceName)) {
                        return (Enum<?>) enumConstant;
                    }
                }
                throw new RuntimeException();
            } else {
                throw new RuntimeException();
            }
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

    /*
     * Custom
     */

    @AllArgsConstructor
    static class ArrearsGenerator implements ICustomGenerator<Arrears, ChBusinessRule> {

//        private final long maxValue;
//        private final long minValue;

        @Override
        public void prepareGenerator(ChBusinessRule rules) {

        }

        @Override
        public Arrears generate() {
            return null;
        }
    }


}
