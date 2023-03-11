package org.laoruga.dtogenerator.generator.builder.builders;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.CustomGenerator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Slf4j
public class CustomGeneratorBuilder implements IGeneratorBuilder<Object> {

    private Supplier<?> dtoInstanceSupplier;
    private Annotation customGeneratorRules;

    public CustomGeneratorBuilder setDtoInstanceSupplier(Supplier<?> dtoInstanceSupplier) {
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        return this;
    }

    public CustomGeneratorBuilder setCustomGeneratorRules(Annotation customGeneratorRules) {
        this.customGeneratorRules = customGeneratorRules;
        return this;
    }

    @Override
    public CustomGenerator build() {
        return new CustomGenerator(createCustomGenerator());
    }

    IGenerator<?> createCustomGenerator() throws DtoGeneratorException {
        CustomRule customRules;
        try {
            customRules = (CustomRule) customGeneratorRules;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error. Unexpected annotation instead of: " + CustomRule.class, e);
        }
        Class<?> generatorClass = null;
        try {
            generatorClass = customRules.generatorClass();
            Object generatorInstance = createInstance(generatorClass);
            if (generatorInstance instanceof ICustomGeneratorArgs) {
                log.debug("Args {} have been obtained from Annotation: {}", Arrays.asList(customRules.args()), customRules);
                ((ICustomGeneratorArgs<?>) generatorInstance).setArgs(customRules.args());
            }
            if (generatorInstance instanceof ICustomGeneratorDtoDependent) {
                setDto(generatorInstance);
            }
            if (generatorInstance instanceof ICustomGenerator) {
                return (ICustomGenerator<?>) generatorInstance;
            } else {
                throw new DtoGeneratorException("Failed to prepare custom generator. " +
                        "Custom generator must implements: '" + ICustomGenerator.class + "' or it's heirs.");
            }
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while preparing custom generator from class: " + generatorClass, e);
        }

    }

    private void setDto(Object generatorInstance) {
        try {
            ((ICustomGeneratorDtoDependent) generatorInstance).setDtoSupplier(dtoInstanceSupplier);
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
