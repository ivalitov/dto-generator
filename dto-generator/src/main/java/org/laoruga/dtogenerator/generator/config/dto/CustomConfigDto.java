package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 27.03.2023
 */
@Builder
@Getter
@Slf4j
public class CustomConfigDto implements ConfigDto {

    private Supplier<?> dtoInstanceSupplier;
    private Annotation customGeneratorRules;
    private IRuleRemark ruleRemark;

    @Override
    public void merge(ConfigDto configDto) {
        throw new NotImplementedException();
    }

    @Override
    public ConfigDto setRuleRemark(IRuleRemark ruleRemark) {
        this.ruleRemark = ruleRemark;
        return this;
    }

    @Override
    public IRuleRemark getRuleRemark() {
        return ruleRemark;
    }

    public IGenerator<?> getCustomGenerator() throws DtoGeneratorException {
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
