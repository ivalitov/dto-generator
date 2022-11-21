package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
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

    int DEFAULT_LEFT_SHIFT_DAYS = 365 * 5;
    int DEFAULT_LEFT_RIGHT_DAYS = 365 * 5;
    IRuleRemark DEFAULT_RULE_REMARK = BasicRuleRemark.RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {LocalDateTime.class};

    int leftShiftDays() default DEFAULT_LEFT_SHIFT_DAYS;

    int rightShiftDays() default DEFAULT_LEFT_RIGHT_DAYS;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;
}
