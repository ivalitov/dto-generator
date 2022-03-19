package laoruga;

import laoruga.dto.DtoVer1;
import laoruga.markup.bounds.DecimalFieldBounds;
import laoruga.markup.bounds.EnumFieldBounds;
import laoruga.markup.bounds.IntFieldBounds;
import laoruga.markup.bounds.StringFieldBounds;
import lombok.AllArgsConstructor;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class DtoBuilder {

    Consumer<DtoVer1> field_1 = dto -> {dto.setFieldString("str");};
    Consumer<DtoVer1> field_2 = dto -> {dto.setFieldInteger(123);};

    public static void main(String[] args) throws IllegalAccessException {
        some();
        DtoVer1 dtoInstance = new DtoVer1();
        DtoBuilder dtoBuilder = new DtoBuilder();
        for (Field field : dtoBuilder.getClass().getDeclaredFields()) {
            if (field.getType() == Consumer.class) {
                if (((ParameterizedTypeImpl) field.getGenericType()).getActualTypeArguments()[0] == DtoVer1.class) {
                Consumer<DtoVer1> fieldConsumer = (Consumer<DtoVer1>) field.get(dtoBuilder);
                fieldConsumer.accept(dtoInstance);
                } else {
                    System.out.println("error " + field);
                }
            } else {
                System.out.println("error " + field);
            }
        }
        System.out.println(dtoInstance);
    }

    static void some() throws IllegalAccessException {
        DtoVer1 dtoInstance = new DtoVer1();
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            Generator<?> generator = selectGenerator(field);
            field.set(dtoInstance, generator.generate());
        }
        System.out.println(dtoInstance);
    }

    static Generator<?> selectGenerator(Field field) {

        if (field.getDeclaringClass() == Double.class) {
            DecimalFieldBounds decimalBounds = field.getAnnotation(DecimalFieldBounds.class);
            if (decimalBounds != null) {
                return new DecimalFieldGenerator(
                        decimalBounds.maxSymbols(),
                        decimalBounds.minSymbols(),
                        decimalBounds.symbolsAfterDot()
                );
            }
        }

        if (field.getDeclaringClass() == String.class) {
            StringFieldBounds stringBounds = field.getAnnotation(StringFieldBounds.class);
            if (stringBounds != null) {
                return new StringFieldGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols(),
                        stringBounds.charset()
                );
            }
        }

        if (field.getDeclaringClass() == Integer.class) {
            IntFieldBounds stringBounds = field.getAnnotation(IntFieldBounds.class);
            if (stringBounds != null) {
                return new IntegerFieldGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols()
                );
            }
        }

        if (field.getDeclaringClass() == Integer.class) {
            EnumFieldBounds stringBounds = field.getAnnotation(EnumFieldBounds.class);
            if (stringBounds != null) {
                return new EnumFieldGenerator(
                        stringBounds.possibleValues(),
                        stringBounds.type()
                );
            }
        }

        return new NullGenerator();
    }

    interface Generator<TYPE> {
        TYPE generate();
    }

    @AllArgsConstructor
    static class DecimalFieldGenerator implements Generator<Double> {

        private final long maxSymbols;
        private final long minSymbols;
        private final int symbolsAfterDot;

        @Override
        public Double generate() {
            return 2d;
        }
    }

    @AllArgsConstructor
    static class StringFieldGenerator implements Generator<String> {

        private final long maxSymbols;
        private final long minSymbols;
        private final CharSet[] charset;

        @Override
        public String generate() {
            return null;
        }
    }

    @AllArgsConstructor
    static class IntegerFieldGenerator implements Generator<Integer> {

        private final long maxSymbols;
        private final long minSymbols;

        @Override
        public Integer generate() {
            return null;
        }
    }

    @AllArgsConstructor
    static class EnumFieldGenerator implements Generator<Integer> {


        @Override
        public Integer generate() {
            return null;
        }
    }

    static class NullGenerator implements Generator<Object> {
        @Override
        public String generate() {
            return null;
        }
    }


}
