package org.laoruga.dtogenerator.api.remarks;

import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.util.dummy.DummyCustomGenerator;

/**
 * The interface is designed to create remarks for custom generation rules.
 *
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface CustomRuleRemark extends IRuleRemark {

    /**
     * Overriding this method specifies the class of custom generator these remarks are intended for.
     * When method overridden, remarks added via {@link DtoGeneratorBuilder#addRuleRemark(CustomRuleRemark)}
     * getting into only generator specified by this method, otherwise - to each custom generator class.
     *
     * @return - class of custom generator for which is intended this 'remark'
     */
    default Class<? extends CustomGenerator<?>> getGeneratorClass() {
        return DummyCustomGenerator.class;
    }

}
