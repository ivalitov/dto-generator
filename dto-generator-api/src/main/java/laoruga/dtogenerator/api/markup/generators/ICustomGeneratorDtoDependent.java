package laoruga.dtogenerator.api.markup.generators;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorDtoDependent<GENERATED_TYPE, DTO_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    void setDto(DTO_TYPE generatedDto);

    boolean isDtoReady();
}
