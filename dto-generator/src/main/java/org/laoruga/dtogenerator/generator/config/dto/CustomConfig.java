package org.laoruga.dtogenerator.generator.config.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 27.03.2023
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CustomConfig implements ConfigDto {

    private CustomGenerator<?> customGenerator;
    private Supplier<?> rootDtoInstanceSupplier;
    private String[] args;
    private String fieldName;

    private static final RuntimeException NOT_IMPLEMENTED = new NotImplementedException(
            "Remarks for custom rules are set via " + DtoGeneratorBuilder.class + " methods." +
                    " CustomConfig isn't supposed to be merged."
    );

    @Override
    public void merge(ConfigDto from) {
        if (from.getClass() == CustomConfig.class) {
            CustomConfig customConfigFrom = (CustomConfig) from;
            if (customConfigFrom.customGenerator != null) this.customGenerator = ((CustomConfig) from).customGenerator;
            if (customConfigFrom.rootDtoInstanceSupplier != null)
                this.rootDtoInstanceSupplier = ((CustomConfig) from).rootDtoInstanceSupplier;
            if (customConfigFrom.args != null) this.args = ((CustomConfig) from).args;
            if (customConfigFrom.fieldName != null) this.fieldName = ((CustomConfig) from).fieldName;
        } else {
            log.debug("Custom config wasn't merged with: '" + from.getClass().getSimpleName() + "'");
        }
    }


    /**
     * @deprecated - rule remarks for custom config provided another way for today
     */
    @Override
    @Deprecated
    public ConfigDto setRuleRemark(RuleRemark ruleRemark) {
        throw NOT_IMPLEMENTED;
    }

    /**
     * @deprecated - rule remarks for custom config provided another way for today
     */
    @Override
    @Deprecated
    public RuleRemark getRuleRemark() {
        throw NOT_IMPLEMENTED;
    }

    public CustomConfig setArgs(String[] args) {
        this.args = args;
        return this;
    }

    public CustomConfig setDtoInstanceSupplier(Supplier<?> rootDtoInstanceSupplier) {
        this.rootDtoInstanceSupplier = rootDtoInstanceSupplier;
        return this;
    }

    public CustomConfig setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }
}
