package org.laoruga.dtogenerator.rule;

import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
public interface RuleInfo {
    Annotation getRule();
    String getGroup();
    Field getField();
    Class<?> getRequiredType();
    boolean isTypesEqual(RuleType ruleType);
}
