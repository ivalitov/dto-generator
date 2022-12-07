package org.laoruga.dtogenerator.rules;

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
public class RuleInfoCollection implements IRuleInfo {
    private IRuleInfo collectionRule;
    private IRuleInfo elementRule;
    private String group;

    public IRuleInfo getElementRule() {
        return Objects.requireNonNull(elementRule, "Item rule wasn't set.");
    }

    @Override
    public Annotation getRule() {
        return collectionRule.getRule();
    }

    public boolean isTypesEqual(RuleType type) {
        return collectionRule.isTypesEqual(type);
    }

    public boolean isElementRulesExist() {
        return elementRule != null;
    }
}
