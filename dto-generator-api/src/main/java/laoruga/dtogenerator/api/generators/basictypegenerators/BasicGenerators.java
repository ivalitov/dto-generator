package laoruga.dtogenerator.api.generators.basictypegenerators;

public class BasicGenerators {

    private BasicGenerators() {}

    public static StringGenerator.StringGeneratorBuilder stringGenerator() {
        return StringGenerator.builder();
    }
}
