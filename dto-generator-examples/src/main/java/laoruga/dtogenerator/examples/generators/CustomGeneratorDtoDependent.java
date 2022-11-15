package laoruga.dtogenerator.examples.generators;

import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.util.RandomUtils;
import laoruga.dtogenerator.examples.dtos.TwoFields;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorDtoDependent implements ICustomGeneratorDtoDependent<String, TwoFields> {

    private TwoFields generatedDto;

    @Override
    public void setDto(TwoFields generatedDto) {
        this.generatedDto = generatedDto;
    }

    @Override
    public boolean isDtoReady() {
        return generatedDto.getSecondField() != null;
    }

    @Override
    public String generate() {
        return "My value dependent on SecondField: '" + generatedDto.getSecondField() + "'";
    }
}
