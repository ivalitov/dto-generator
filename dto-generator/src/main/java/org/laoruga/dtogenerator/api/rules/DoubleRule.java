package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(DoubleRules.class)
public @interface DoubleRule {

    double DEFAULT_MIN = 0D;
    double DEFAULT_MAX = 999999999999999999D;
    int DEFAULT_PRECISION = 2;
    IRuleRemark DEFAULT_RULE_REMARK = BasicRuleRemark.RANDOM_VALUE;

    double maxValue() default DEFAULT_MAX;

    double minValue() default DEFAULT_MIN;

    int precision() default 2;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    Class<?> generatedType() default Double.class;
}