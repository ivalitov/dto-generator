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
public @interface DoubleRules {

    double DEFAULT_MIN = 0D;
    double DEFAULT_MAX = 999999999999999999D;
    int DEFAULT_PRECISION = 2;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;

    double maxValue() default DEFAULT_MAX;

    double minValue() default DEFAULT_MIN;

    int precision() default 2;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;
}