package laoruga.dtogenerator.api.generators.basictypegenerators;

/**
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */

public class BasicGeneratorsBuilders {

    private BasicGeneratorsBuilders() {
    }

    public static StringGenerator.StringGeneratorBuilder stringBuilder() {
        return StringGenerator.builder();
    }

    public static DoubleGenerator.DoubleGeneratorBuilder doubleBuilder() {
        return DoubleGenerator.builder();
    }

    public static IntegerGenerator.IntegerGeneratorBuilder integerBuilder() {
        return IntegerGenerator.builder();
    }

    public static LongGenerator.LongGeneratorBuilder longBuilder() {
        return LongGenerator.builder();
    }

    public static EnumGenerator.EnumGeneratorBuilder enumBuilder() {
        return EnumGenerator.builder();
    }

    public static ListGenerator.ListGeneratorBuilder<?> listBuilder() {
        return ListGenerator.builder();
    }

    public static LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder localDateTimeBuilder() {
        return LocalDateTimeGenerator.builder();
    }
}
