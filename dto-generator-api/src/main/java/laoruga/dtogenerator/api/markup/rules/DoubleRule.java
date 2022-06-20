package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.meta.Rule;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.RANDOM_VALUE;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(DoubleRules.class)
public @interface DoubleRule {

    double DEFAULT_MIN = 0D;
    double DEFAULT_MAX = 999999999999999999D;
    int DEFAULT_PRECISION = 2;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {Double.class, Double.TYPE};

    double maxValue() default DEFAULT_MAX;

    double minValue() default DEFAULT_MIN;

    int precision() default 2;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;

    String group() default DEFAULT;
}