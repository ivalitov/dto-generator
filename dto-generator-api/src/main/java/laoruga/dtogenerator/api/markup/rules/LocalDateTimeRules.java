package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.RANDOM_VALUE;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface LocalDateTimeRules {

    int DEFAULT_LEFT_SHIFT_DAYS = 365 * 5;
    int DEFAULT_LEFT_RIGHT_DAYS = 365 * 5;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {LocalDateTime.class};

    int leftShiftDays() default DEFAULT_LEFT_SHIFT_DAYS;

    int rightShiftDays() default DEFAULT_LEFT_RIGHT_DAYS;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;
}
