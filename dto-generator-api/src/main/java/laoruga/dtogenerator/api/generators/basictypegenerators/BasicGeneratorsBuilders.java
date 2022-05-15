package laoruga.dtogenerator.api.generators.basictypegenerators;

public class BasicGeneratorsBuilders {

    private BasicGeneratorsBuilders() {
    }

    public static StringGenerator.StringGeneratorBuilder stringGenerator() {
        return StringGenerator.builder();
    }
}
