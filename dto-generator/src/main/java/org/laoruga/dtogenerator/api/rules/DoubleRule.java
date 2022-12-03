package org.laoruga.dtogenerator.api.rules;

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

    double maxValue() default 999999999999999999D;

    double minValue() default 0D;

    int precision() default 2;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    Class<?> generatedType() default Double.class;
}