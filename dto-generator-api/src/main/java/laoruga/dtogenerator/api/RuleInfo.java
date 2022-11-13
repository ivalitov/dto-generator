package laoruga.dtogenerator.api;

import lombok.Data;
import lombok.ToString;

import java.lang.annotation.Annotation;

/**
 * @author Il'dar Valitov
 * Created on 10.11.2022
 */
@Data
@ToString
class RuleInfo implements IRuleInfo {
    private Annotation rule;
    private RuleType ruleType;
    private boolean multipleRules;
    private String group;

    public static RuleInfoBuilder builder(){
        return new RuleInfoBuilder();
    }
    public boolean isTypesEqual(RuleType type) {
        return ruleType == type;
    }

}
