package dtogenerator.api.markup.generators;

public interface ICustomGenerator<GENERATED_TYPE> extends IGenerator<GENERATED_TYPE> {
    void setArgs(String[] args);
}