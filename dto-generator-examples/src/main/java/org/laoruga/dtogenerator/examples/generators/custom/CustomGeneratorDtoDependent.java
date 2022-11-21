package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.api.generators.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.examples.dto.TwoFields;

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
