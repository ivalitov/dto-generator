package laoruga.dtogenerator.api.markup.generators;

import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorRemarkable<T> extends ICustomGenerator<T> {

    void setRuleRemarks(List<CustomRuleRemarkWrapper> iRuleRemarks);

}
