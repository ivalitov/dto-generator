package org.laoruga.dtogenerator.functional.data.customgenerator;

import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemark;

/**
 * @author Il'dar Valitov
 * Created on 07.02.2023
 */
public enum RemarkNonArgs implements CustomRuleRemark {
    NULL_VALUE;

    @Override
    public Class<? extends CustomGenerator<?>> getGeneratorClass() {
        return ClientInfoGenerator.class;
    }
}