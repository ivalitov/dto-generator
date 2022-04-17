package laoruga.dtogenerator.api.markup.generators;

public interface ICustomGeneratorArgs<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {
    void setArgs(String[] args);
}
