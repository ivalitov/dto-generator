package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.RuleRemark;

/**
 * Custom generators based on this interface are able to have {@link RuleRemark} parameter.
 * For example, each {@link org.laoruga.dtogenerator.api.rules.meta.Rule Rule} annotation has
 * {@link org.laoruga.dtogenerator.constants.Boundary boundary} parameter.
 * There are several ways of passing arguments:
 *
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorRemark<T> extends CustomGenerator<T> {

    void setRuleRemark(RuleRemark ruleRemark);
}
