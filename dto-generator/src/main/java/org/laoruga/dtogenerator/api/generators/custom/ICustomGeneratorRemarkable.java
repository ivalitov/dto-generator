package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWrapper;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorRemarkable<T> extends ICustomGenerator<T> {

    void setRuleRemarks(List<CustomRuleRemarkWrapper> iRuleRemarks);

}
