package laoruga.dtogenerator.api.generators.basictypegenerators;

public class BasicGeneratorsBuilders {

    private BasicGeneratorsBuilders() {
    }

    public static StringGenerator.StringGeneratorBuilder string() {
        return StringGenerator.builder();
    }
}
