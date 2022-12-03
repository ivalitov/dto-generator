package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.BoundType;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD, TYPE_USE})
@Rule
@Repeatable(IntegerRules.class)
public @interface IntegerRule {

    @BoundType(BasicRuleRemark.MAX_VALUE)
    int maxValue() default 999999999;

    @BoundType(BasicRuleRemark.MIN_VALUE)
    int minValue() default 0;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    Class<?> generatedType() default Integer.class;
}
