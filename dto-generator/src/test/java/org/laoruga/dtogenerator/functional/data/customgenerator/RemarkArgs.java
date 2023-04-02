package org.laoruga.dtogenerator.functional.data.customgenerator;

import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkArgs;

/**
 * @author Il'dar Valitov
 * Created on 07.02.2023
 */
public enum RemarkArgs implements CustomRuleRemarkArgs {
    NULL_VALUE;

    @Override
    public Class<? extends CustomGenerator<?>> getGeneratorClass() {
        return ClientInfoGenerator.class;
    }

    @Override
    public int requiredArgsNumber() {
        return 0;
    }
}