package org.laoruga.dtogenerator.rule;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;


/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Setter(AccessLevel.PACKAGE)
@Getter
@Builder
public class RuleInfoCollection implements IRuleInfo {
    private IRuleInfo collectionRuleInfo;
    private IRuleInfo elementRuleInfo;
    private Field field;
    private Class<?> elementType;
    private String group;

    public IRuleInfo getElementRuleInfo() {
        return Objects.requireNonNull(elementRuleInfo, "Element rule wasn't set.");
    }

    @Override
    public Annotation getRule() {
        return collectionRuleInfo.getRule();
    }

    @Override
    public Class<?> getRequiredType() {
        return field.getType();
    }

    public boolean isTypesEqual(RuleType type) {
        return collectionRuleInfo.isTypesEqual(type);
    }

    public boolean isElementRulesExist() {
        return elementRuleInfo != null;
    }
}
