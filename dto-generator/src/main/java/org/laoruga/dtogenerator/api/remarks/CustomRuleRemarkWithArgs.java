package org.laoruga.dtogenerator.api.remarks;

import lombok.Value;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Value
public class CustomRuleRemarkWithArgs {

    ICustomRuleRemark customRuleRemark;
    Class<? extends ICustomGenerator<?>> generatorClass;
    String[] args;
}