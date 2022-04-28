package laoruga.dtogenerator.api.markup.generators;

public interface ICollectionGenerator<GENERATED_TYPE> extends IGenerator<GENERATED_TYPE> {

    IGenerator<?> getInnerGenerator();
}
