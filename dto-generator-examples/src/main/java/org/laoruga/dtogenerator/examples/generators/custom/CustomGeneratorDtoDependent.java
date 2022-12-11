package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.examples.dto.TwoFields;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorDtoDependent implements ICustomGeneratorDtoDependent<String, TwoFields> {

    private Supplier<TwoFields> generatedDtoSupplier;

    @Override
    public void setDtoSupplier(Supplier<TwoFields> generatedDto) {
        this.generatedDtoSupplier = generatedDto;
    }

    @Override
    public boolean isDtoReady() {
        return generatedDtoSupplier.get().getSecondField() != null;
    }

    @Override
    public String generate() {
        return "My value dependent on SecondField: '" + generatedDtoSupplier.get().getSecondField() + "'";
    }
}
