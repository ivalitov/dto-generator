package org.laoruga.dtogenerator.functional.data.customgenerator;

import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemarkArgs;

/**
 * @author Il'dar Valitov
 * Created on 07.02.2023
 */
public enum RemarkArgs implements ICustomRuleRemarkArgs {
    NULL_VALUE;

    @Override
    public Class<? extends ICustomGenerator<?>> getGeneratorClass() {
        return ClientInfoGenerator.class;
    }

    @Override
    public int requiredArgsNumber() {
        return 0;
    }
}