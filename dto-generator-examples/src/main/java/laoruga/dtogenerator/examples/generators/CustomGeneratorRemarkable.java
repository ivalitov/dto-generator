package laoruga.dtogenerator.examples.generators;

import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorRemarkable implements ICustomGeneratorRemarkable<String> {
    private List<CustomRuleRemarkWrapper> ruleRemarks;

    @Override
    public void setRuleRemarks(List<CustomRuleRemarkWrapper> ruleRemarks) {
        this.ruleRemarks = ruleRemarks;
    }

    @Override
    public String generate() {
        return null;
    }
}
