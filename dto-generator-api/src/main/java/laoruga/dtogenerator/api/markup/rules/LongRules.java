package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.RANDOM_VALUE;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface LongRules {

    long DEFAULT_MIN = 0L;
    long DEFAULT_MAX = 999999999999999999L;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;

    long minValue() default DEFAULT_MIN;

    long maxValue() default DEFAULT_MAX;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;
}
