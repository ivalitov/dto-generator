package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDateTime;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(LocalDateTimeRules.class)
public @interface LocalDateTimeRule {

    int leftShiftDays() default 365 * 5;

    int rightShiftDays() default 365 * 5;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    Class<?> generatedType() default LocalDateTime.class;
}
