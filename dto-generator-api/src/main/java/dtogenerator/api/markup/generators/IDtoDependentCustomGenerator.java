package dtogenerator.api.markup.generators;

public interface IDtoDependentCustomGenerator<GENERATED_TYPE, DTO_TYPE> extends IGenerator<GENERATED_TYPE> {

    void setDto(DTO_TYPE generatedDto);

    boolean isDtoReady();

}
