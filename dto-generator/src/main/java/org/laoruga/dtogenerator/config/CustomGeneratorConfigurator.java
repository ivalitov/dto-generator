package org.laoruga.dtogenerator.config;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.custom.*;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 29.03.2023
 */
@Slf4j
@Builder(builderClassName = "Builder")
public class CustomGeneratorConfigurator {

    private String[] args;
    private Supplier<?> dtoInstanceSupplier;
    private RemarksHolder remarksHolder;
    private RuleRemark ruleRemark;
    private String fieldName;

    public static class Builder {

        public Builder merge(Builder builder) {
            if (builder.args != null) this.args = builder.args;
            if (builder.ruleRemark != null) this.ruleRemark = builder.ruleRemark;
            return this;
        }

    }

    public void configure(CustomGenerator<?> generatorInstance) {
        try {
            if (generatorInstance instanceof CustomGeneratorArgs) {
                log.debug("Custom generator args: ' " + (args != null ? Arrays.asList(args) : "") + " ' have been obtained.");
                ((CustomGeneratorArgs<?>) generatorInstance).setArgs(args);
            }
            if (generatorInstance instanceof CustomGeneratorDtoDependent) {
                setDto(generatorInstance);
            }
            if (generatorInstance instanceof CustomGeneratorConfigMap) {


                ((CustomGeneratorConfigMap<?>) generatorInstance).setConfigMap(
                        remarksHolder
                                .getCustomRemarks()
                                .getConfigMap(fieldName, generatorInstance.getClass())
                );


            } else if (generatorInstance instanceof CustomGeneratorRemarks) {

                IRuleRemark maybeRuleRemark = remarksHolder
                        .getBasicRemarks()
                        .getBasicRuleRemarkOrNull(fieldName);

                ((CustomGeneratorRemarks<?>) generatorInstance).setRuleRemark(
                        maybeRuleRemark != null ? maybeRuleRemark : ruleRemark
                );

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
