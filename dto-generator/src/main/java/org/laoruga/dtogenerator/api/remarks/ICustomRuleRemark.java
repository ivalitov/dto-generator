package org.laoruga.dtogenerator.api.remarks;

import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;

/**
 * The interface is designed to create remarks for custom generation rules.
 *
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ICustomRuleRemark {

    /**
     * @return class of custom generator for which is intended this 'remark'
     */
    Class<? extends ICustomGenerator<?>> getGeneratorClass();

}
