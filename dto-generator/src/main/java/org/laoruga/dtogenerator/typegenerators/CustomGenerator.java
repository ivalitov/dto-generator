package org.laoruga.dtogenerator.typegenerators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.*;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 25.11.2022
 */
@AllArgsConstructor
public class CustomGenerator implements IGenerator<Object> {

    @Getter
    private final IGenerator<?> usersGeneratorInstance;

    public static CustomGeneratorBuilder builder() {
        return new CustomGeneratorBuilder();
    }

    @Override
    public Object generate() {
        return usersGeneratorInstance.generate();
    }

    @Slf4j
    public static class CustomGeneratorBuilder implements IGeneratorBuilder {

        private Object dtoInstance;
        private Annotation customGeneratorRules;

        public CustomGeneratorBuilder setDtoInstance(Object dtoInstance) {
            this.dtoInstance = dtoInstance;
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
                ((ICustomGeneratorDtoDependent) generatorInstance).setDto(dtoInstance);
            } catch (ClassCastException e) {
                throw new DtoGeneratorException("ClassCastException while trying to set basic DTO into " +
                        "DTO dependent custom generator. Perhaps there is wrong argument type is passing into " +
                        "'setDto' method of generator class. " +
                        "Generator class: '" + generatorInstance.getClass() + "', " +
                        "Passing argument type: '" + dtoInstance.getClass() + "'", e);
            } catch (Exception e) {
                throw new DtoGeneratorException("Exception was thrown while trying to set DTO into " +
                        "DTO dependent custom generator: " + generatorInstance.getClass(), e);
            }
        }
    }

}
