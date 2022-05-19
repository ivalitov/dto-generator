package laoruga.dtogenerator.api.generators.basictypegenerators;

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
}
