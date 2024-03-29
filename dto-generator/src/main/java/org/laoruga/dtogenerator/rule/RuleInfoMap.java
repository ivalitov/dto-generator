package org.laoruga.dtogenerator.rule;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.laoruga.dtogenerator.api.rules.MapRule;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.reflect.Field;
import java.util.Objects;


/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Setter(AccessLevel.PACKAGE)
@Getter
@Builder
public class RuleInfoMap implements RuleInfo {
    private RuleInfoSimple mapRule;
    private RuleInfoSimple keyRule;
    private RuleInfoSimple valueRule;

    private Field field;
    private String group;

    public RuleInfo getValueRule() {
        return Objects.requireNonNull(valueRule, "Value rule wasn't set.");
    }

    public RuleInfo getKeyRule() {
        return Objects.requireNonNull(keyRule, "Key rule wasn't set.");
    }

    @Override
    public MapRule getRule() {
        return (MapRule) mapRule.getRule();
    }

    @Override
    public Class<?> getRequiredType() {
        return field.getType();
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
