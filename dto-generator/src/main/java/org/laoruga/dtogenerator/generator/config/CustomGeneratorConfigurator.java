package org.laoruga.dtogenerator.generator.config;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 29.03.2023
 */
@Slf4j
@Builder
public class CustomGeneratorConfigurator {

    private String[] args;
    private Supplier<?> dtoInstanceSupplier;

    public void configure(CustomGenerator<?> generatorInstance) {
        try {
            if (generatorInstance instanceof CustomGeneratorArgs) {
                log.debug("Custom generator args: ' " + Arrays.asList(args) + " ' have been obtained.");
                ((CustomGeneratorArgs<?>) generatorInstance).setArgs(args);
            }
            if (generatorInstance instanceof CustomGeneratorDtoDependent) {
                setDto(generatorInstance);
            }
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while preparing custom generator.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setDto(Object generatorInstance) {
        try {
            ((CustomGeneratorDtoDependent) generatorInstance).setDtoSupplier(dtoInstanceSupplier);
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("ClassCastException while trying to set basic DTO into " +
                    "DTO dependent custom generator. Perhaps there is wrong argument type is passing into " +
                    "'setDto' method of generator class. " +
                    "Generator class: '" + generatorInstance.getClass() + "', " +
                    "Passing argument type: '" + dtoInstanceSupplier.getClass() + "'", e);
        } catch (Exception e) {
            throw new DtoGeneratorException("Exception was thrown while trying to set DTO into " +
                    "DTO dependent custom generator: " + generatorInstance.getClass(), e);
        }
    }
}
