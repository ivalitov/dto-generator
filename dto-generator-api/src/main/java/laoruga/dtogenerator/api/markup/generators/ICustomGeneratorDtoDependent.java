package laoruga.dtogenerator.api.markup.generators;

public interface ICustomGeneratorDtoDependent<GENERATED_TYPE, DTO_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setDto(DTO_TYPE generatedDto);

    boolean isDtoReady();

}
