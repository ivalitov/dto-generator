package org.laoruga.dtogenerator.generator.config.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 27.03.2023
 */
@Builder
@Getter
@Slf4j
public class CustomConfig implements ConfigDto {

    private CustomGenerator<?> customGenerator;

    private static final RuntimeException NOT_IMPLEMENTED =
            new NotImplementedException(
                    "Remarks for custom rules are set via " + DtoGeneratorBuilder.class + " methods." +
                            " CustomConfig isn't supposed to be merged."
            );

    /**
     * @deprecated - configs are not applicable for Custom generators for today
     */
    @Override
    @Deprecated
    public void merge(ConfigDto from) {
        throw NOT_IMPLEMENTED;
    }


    /**
     * @deprecated - rule remarks for custom config provided another way for today
     */
    @Override
    @Deprecated
    public ConfigDto setRuleRemark(IRuleRemark ruleRemark) {
        throw NOT_IMPLEMENTED;
    }

    /**
     * @deprecated - rule remarks for custom config provided another way for today
     */
    @Override
    @Deprecated
    public IRuleRemark getRuleRemark() {
        throw NOT_IMPLEMENTED;
    }

}
