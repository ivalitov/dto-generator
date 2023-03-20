package org.laoruga.dtogenerator.rule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;
import java.util.Objects;


/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Setter(AccessLevel.PACKAGE)
@Getter
public class RuleInfoMap implements IRuleInfo {
    private IRuleInfo mapRule;
    private IRuleInfo keyRule;
    private IRuleInfo valueRule;
    private String group;

    public IRuleInfo getValueRule() {
        return Objects.requireNonNull(valueRule, "Value rule wasn't set.");
    }

    public IRuleInfo getKeyRule() {
        return Objects.requireNonNull(keyRule, "Key rule wasn't set.");
    }

    @Override
    public Annotation getRule() {
        return mapRule.getRule();
    }

    public boolean isTypesEqual(RuleType type) {
        return mapRule.isTypesEqual(type);
    }

    public boolean isKeyRulesExist() {
        return keyRule != null;
    }

    public boolean isValueRulesExist() {
        return valueRule != null;
    }
}
