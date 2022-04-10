package dtogenerator.api.markup.generators;

public interface IGenerator<GENERATED_TYPE> {
    GENERATED_TYPE generate();
}
